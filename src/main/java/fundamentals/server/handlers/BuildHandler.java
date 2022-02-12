package fundamentals.server.handlers;

import fundamentals.server.BuildStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import java.io.IOException;

public class BuildHandler extends AbstractHandler {

    private final BuildStorage storage;

    /**
     * Create a new handler for /handler/
     *
     * @param storage the build storage to use for the request
     */
    public BuildHandler(BuildStorage storage) {
        this.storage = storage;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        if (target.equals("/")) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
            response.getWriter().println("Please provide a build ID: /build/{id}");
        } else {
            String buildID = target.substring(1);
            System.out.println("Build ID: " + buildID);

            // Find the JSON object that has information about the requested build ID and return that.
            JSONObject build = storage.getBuild(buildID);
            if (build == null) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("A build with the ID \"" + buildID + "\" does not exist");
            } else {
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(build.toString());
            }

            baseRequest.setHandled(true);
        }
    }
}
