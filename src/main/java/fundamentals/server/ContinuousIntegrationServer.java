package fundamentals.server;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class ContinuousIntegrationServer extends AbstractHandler {

    final static int DEFAULT_PORT_NUMBER = 8014;

    static int getPortNumberFromInputOrElseDefault(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.err.println("Unparsable port number");
            System.out.println("usage: ci-server <port>");
        } catch (ArrayIndexOutOfBoundsException ex) {
            return DEFAULT_PORT_NUMBER;
        }
        return DEFAULT_PORT_NUMBER;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Starting up server...");
        var portNumber = getPortNumberFromInputOrElseDefault(args);
        var server = new Server(portNumber);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        System.out.println("Server has successfully started on port " + portNumber);
        try {
            server.join();
        } catch (InterruptedException ex) {
            System.err.println("Interrupted, gracefully shutting down...");
            throw ex;
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        if (baseRequest.getMethod().equals("POST")){
            String payload = request.getParameter("payload");
            if (payload != null){
                try {
                    AutomatedTests autoTests = new AutomatedTests(payload);
                } catch (Exception e) {
                    System.out.println("Got exception!");
                    e.printStackTrace();
                }
            }
        }

        System.out.println(target);

        response.getWriter().println("CI job done");
    }
}
