package fundamentals.server.handlers;

import fundamentals.server.BuildStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;

import java.io.IOException;

/**
 * A handler that responds with a JSON-object with all builds performed by the
 * server.
 */
public class BuildAllHandler extends AbstractHandler {

    private final BuildStorage storage;

    public BuildAllHandler(BuildStorage storage) {
        this.storage = storage;
    }

    /**
     * Responds to a request with a JSON string representing all performed builds.
     */
    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        // Respond with the JSON array of all builds.
        JSONArray builds = storage.getAllBuilds();
        response.getWriter().println(builds.toString());
    }
}
