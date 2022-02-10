package fundamentals.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WebhookHandler extends AbstractHandler {

    private final Environment environment;

    public WebhookHandler(Environment environment) {
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

            String commitSha = root.getString("after");
            String repository = root.getJSONObject("repository").getString("name");
            String owner = root.getJSONObject("repository").getJSONObject("owner").getString("name");

            System.out.println("Push event: ");
            System.out.println("Commit:" + commitSha);
            System.out.println("Repository:" + repository);
            System.out.println("Owner:" + owner);

            // TODO:
            // Create a build ID, build date and set build status = pending. Store this in a JSON file and keep it in main-memory.

            // Set the commit status to pending on Github.
            String username = environment.getValue("USERNAME");
            String personalAccessToken  = environment.getValue("PERSONAL_ACCESS_TOKEN");
            GithubCommitAPI api = new GithubCommitAPI(owner, repository, commitSha, username, personalAccessToken);
            GithubCommitAPIRequest apiRequest = api.setCommitStatusPending("Compiling and running tests...", "");

            if (apiRequest.send()) {
                System.out.println("Updated commit status to pending for commit: " + commitSha);
            } else {
                System.out.println("Failed to update commit status for: " + commitSha);
            }

            // On a background thread compile and test the repo.
            // When the background thread is done, update build logs, build status, etc in the JSON file and main memory.
            // Update the commit status on Github.
        }
    }
}
