package fundamentals.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;

import java.io.IOException;

public class BuildAllHandler extends AbstractHandler {

    private final BuildStorage storage;

    public BuildAllHandler(BuildStorage storage) {
        this.storage = storage;
    }

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
