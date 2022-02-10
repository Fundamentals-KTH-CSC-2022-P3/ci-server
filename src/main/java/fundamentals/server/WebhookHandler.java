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

    private final BuildStorage storage;

    public WebhookHandler(BuildStorage storage) {
        this.storage = storage;
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

            String commitHash = root.getString("after");
            String repository = root.getJSONObject("repository").getString("name");
            String owner = root.getJSONObject("repository").getJSONObject("owner").getString("name");

            System.out.println("Push event: ");
            System.out.println("Commit:" + commitHash);
            System.out.println("Repository:" + repository);
            System.out.println("Owner:" + owner);

            // Create a build ID, build date and set build status = pending. Store this in a JSONObject in main-memory.
            JSONObject newBuild = storage.addNewBuild(commitHash, repository, owner);

            // TODO:
            // Set the commit status to pending on Github.
            // On a background thread compile and test the repo.
            // When the background thread is done, update build logs, build status, etc in the JSONObject (main-memory).
            // Update the commit status on Github.
            // Write the JSONObject to disk using the saveToDisk() method in the Storage class.
        }
    }
}
