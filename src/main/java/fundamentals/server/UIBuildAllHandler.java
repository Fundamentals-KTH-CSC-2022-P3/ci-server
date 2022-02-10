package fundamentals.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UIBuildAllHandler extends AbstractHandler {
    private static final String UI_BUILD_FILE = "public/build_all.html";

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        try (BufferedReader reader = new BufferedReader(new FileReader(UI_BUILD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.getWriter().println(line);
            }
        }
    }
}
