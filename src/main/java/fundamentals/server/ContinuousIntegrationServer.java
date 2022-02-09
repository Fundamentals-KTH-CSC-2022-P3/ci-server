package fundamentals.server;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;


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

    static ContextHandler getContextHandler(String path, Handler handler) {
        var context = new ContextHandler();
        context.setContextPath(path);
        context.setHandler(handler);
        return context;
    }

    static ContextHandlerCollection getEndpointsHandler() {
        var endpoints = new ContextHandlerCollection();
        endpoints.addHandler(getContextHandler("/webhook", new WebhookHandler()));
        endpoints.addHandler(getContextHandler("/build/all", new BuildAllHandler()));
        endpoints.addHandler(getContextHandler("/build", new BuildHandler()));
        return endpoints;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting up server...");
        var portNumber = getPortNumberFromInputOrElseDefault(args);
        var server = new Server(portNumber);
        server.setHandler(getEndpointsHandler());
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
            throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String payload = "";
        if (baseRequest.getMethod().equals("POST")) {
            payload = request.getParameter("payload");
            if (payload == null)
                return;
        }
        RepoManager repoManager;
        try {
            repoManager = new RepoManager(payload);
        } catch (Exception e) {
            System.out.println("Got exception!");
            e.printStackTrace();
            return;
        }

        repoManager.cloneRepo();
        TestRunner testRunner = new TestRunner(repoManager);
        testRunner.run();
        repoManager.cleanUp();

        System.out.println(target);

        response.getWriter().println("CI job done");
    }
}
