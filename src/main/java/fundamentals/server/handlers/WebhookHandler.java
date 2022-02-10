package fundamentals.server.handlers;


import fundamentals.server.Environment;
import fundamentals.server.SecurityManager;
import fundamentals.server.Tester;
import fundamentals.server.gitTooling.GithubCommitAPI;
import fundamentals.server.gitTooling.GithubCommitAPIRequest;
import fundamentals.server.gitTooling.RepoManager;
import fundamentals.server.helpers.Bash;
import fundamentals.server.helpers.Compiler;


import fundamentals.server.BuildStorage;

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
        }
        else if (event.equals("push")) {
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            // Read the body and parse it to a JSON object.
            StringBuilder body = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            JSONObject root = new JSONObject(body.toString());

            String owner = root.getJSONObject("repository").getJSONObject("owner").getString("name");
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
            JSONObject newBuild = storage.addNewBuild(owner, repository, commitHash);
            String buildID = newBuild.getString("build_id");

            String username = environment.getValue("USERNAME");
            String personalAccessToken = environment.getValue("PERSONAL_ACCESS_TOKEN");
            GithubCommitAPI api = new GithubCommitAPI(owner, repository, commitHash, username, personalAccessToken);
            GithubCommitAPIRequest apiRequest = api.setCommitStatusPending("Compiling and running tests...", "http://localhost/build/" + buildID);

            if (apiRequest.send()) {
                System.out.println("Updated commit status to pending for commit: " + commitHash);
            } else {
                System.out.println("Failed to update commit status for: " + commitHash);
            }

            RepoManager manager = new RepoManager(body.toString(), environment);
            Compiler compiler = new Compiler(manager.getRepoDir(), new Bash());
            Tester tester = new Tester(manager.getRepoDir(), new Bash());

            manager.cloneRepo();
            manager.checkoutBranch();

            Boolean didCompile = compiler.compile();

            if (didCompile) {
                System.out.println("Did compile without error");
            } else {
                System.err.println("compilation failed");
            }

            Boolean testsPassed = tester.run();

            if (testsPassed) {
                System.out.println("testsuite executed without any failures");
            } else {
                System.err.println("testsuite failed");
            }

            manager.cleanUp();

            // TODO:
            // On a background thread compile and test the repo.
            // When the background thread is done, update build logs, build status, etc in the JSONObject (main-memory).
            // Update the commit status on Github.
            // Write the JSONObject to disk using the saveToDisk() method in the Storage class.
        }
    }
}