package fundamentals.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;


public class ContinuousIntegrationServer {

    final static int DEFAULT_PORT_NUMBER = 8014;
    final static BuildStorage storage = BuildStorage.loadBuildStorageFile();

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
        endpoints.addHandler(getContextHandler("/webhook", new WebhookHandler(storage)));
        endpoints.addHandler(getContextHandler("/build/all", new BuildAllHandler(storage)));
        endpoints.addHandler(getContextHandler("/build", new BuildHandler(storage)));
        endpoints.addHandler(getContextHandler("/ui/build", new UIBuildHandler()));
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
}
