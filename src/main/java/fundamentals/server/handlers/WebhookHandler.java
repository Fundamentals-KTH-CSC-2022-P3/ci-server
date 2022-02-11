package fundamentals.server.handlers;


import fundamentals.server.BuildStorage;
import fundamentals.server.Environment;
import fundamentals.server.SecurityManager;
import fundamentals.server.Tester;
import fundamentals.server.gitTooling.GithubCommitAPI;
import fundamentals.server.gitTooling.GithubCommitAPIRequest;
import fundamentals.server.gitTooling.RepoManager;
import fundamentals.server.helpers.Bash;
import fundamentals.server.helpers.Compiler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The {@code WebhookHandler} will handle requests on the /webhook endpoint. These requests will come from Github
 * and indicates that an event has occurred. We are especially interested in listening to the "push" event which is when a
 * new commit has been pushed.
 */
public class WebhookHandler extends AbstractHandler {

    private final BuildStorage storage;
    private final Environment environment;

    public WebhookHandler(Environment environment, BuildStorage storage) {
        this.storage = storage;
        this.environment = environment;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        // We probably want to do som validation here to ensure that we
        // got this request from Github and so on...

        String event = baseRequest.getHeader("X-GitHub-Event");

        // Something is wrong Github should include this HTTP header.
        if (event == null)
            return;

        // Listen for interesting events.
        if (event.equals("ping")) {
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            System.out.println("Ping event");
        } else if (event.equals("push")) {
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            // Read the body and parse it to a JSON object.
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            JSONObject root = new JSONObject(body.toString());

            String owner = root.getJSONObject("repository").getJSONObject("owner").getString("name");
            String branch = root.getString("ref").substring("refs/heads/".length());
            String repository = root.getJSONObject("repository").getString("name");

            try {
                var repositoryURI = URI.create(root.getJSONObject("repository").getString("url"));
                SecurityManager.getInstance().verifyAgainstWhitelist(repositoryURI);
            } catch (SecurityException | IllegalArgumentException | NullPointerException ex) {
                System.err.println(ex.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String commitHash = root.getString("after");

            System.out.println("Push event: ");
            System.out.println("Owner:" + owner);
            System.out.println("Repository:" + repository);
            System.out.println("Commit:" + commitHash);

            // Create a build ID, build date and set build status = pending. Store this in a JSONObject in main-memory.
            JSONObject newBuild = storage.addNewBuild(owner, repository, branch, commitHash);
            String buildID = newBuild.getString("build_id");

            String username = environment.getValue("USERNAME");
            String personalAccessToken = environment.getValue("PERSONAL_ACCESS_TOKEN");
            String hostname = environment.getValue("HOSTNAME");

            // The URL Github will show the user associated with the Github status.
            String targetUrl = "http://" + hostname + "/build/" + buildID;

            GithubCommitAPI api = new GithubCommitAPI(owner, repository, commitHash, username, personalAccessToken);
            GithubCommitAPIRequest apiRequest = api.setCommitStatusPending("Compiling and running tests...", targetUrl);

            if (apiRequest.send()) {
                System.out.println("Updated commit status to pending for commit: " + commitHash);
            } else {
                System.out.println("Failed to update commit status for: " + commitHash);
            }

            RepoManager manager = new RepoManager(body.toString(), environment);
            Compiler compiler = new Compiler(manager.getRepoDir(), new Bash());

            // Clone the repo and switch to the modified branch.
            manager.cloneRepo();
            manager.checkoutBranch();

            // Check if the project compiles.
            Boolean didCompile = compiler.compile();

            // Store compile logs in the JSON file.
            for (String log : compiler.getCompileOutput())
                newBuild.getJSONArray("compile_logs").put(log);

            if (didCompile) {
                System.out.println("Did compile without error");

                newBuild.put("compile_status", "success");

                // Check if all tests pass.
                Tester tester = new Tester(manager.getRepoDir(), new Bash());
                Boolean testsPassed = tester.run();

                // Store test logs in the JSON file.
                for (String log : tester.getTestOutput())
                    newBuild.getJSONArray("test_logs").put(log);

                if (testsPassed) {
                    System.out.println("Testsuite executed without any failures");
                    newBuild.put("test_status", "success");
                    apiRequest = api.setCommitStatusSuccess("Tests passed", targetUrl);
                } else {
                    System.out.println("Testsuite failed");
                    newBuild.put("test_status", "failure");
                    apiRequest = api.setCommitStatusFailure("Tests failed", targetUrl);
                }
            } else {
                System.err.println("Compilation failed");
                newBuild.put("compile_status", "error");
                newBuild.put("test_status", "failure"); // No need to run the tests if the project did not compile.
                apiRequest = api.setCommitStatusError("Compile error", targetUrl);
            }

            // The build has finished store the time and save the build to disk.
            newBuild.put("build_ended", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            storage.saveToDisk();

            // Update the commit status for the commit on Github.
            if (apiRequest.send()) {
                System.out.println("Updated commit status for commit: " + commitHash);
            } else {
                System.out.println("Failed to update commit status for: " + commitHash);
            }

            // Remove the cloned repo from disk (no need to store this anymore because the build has completed).
            manager.cleanUp();
        }
    }
}
