package fundamentals.server;

import fundamentals.server.handlers.BuildAllHandler;
import fundamentals.server.handlers.BuildHandler;
import fundamentals.server.handlers.WebhookHandler;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.security.Constraint;

import java.util.Collections;

/**
 * The main class of the CI-server. Handles starting the server and setting up the handlers for the endpoints.
 */
public class ContinuousIntegrationServer {

    final static int DEFAULT_PORT_NUMBER = 8014;
    final static Environment environment = Environment.loadEnvironmentFile();
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


    /**
     * Wraps and returns the provided handler in a context handler with a set path
     * @param path the relative path to which the context should be bound to.
     * @param handler An already initialized handler which is to be wrapped
     */
    static Handler getContextHandler(String path, Handler handler) {
        var context = new ContextHandler();
        context.setContextPath(path);
        context.setHandler(handler);
        return context;
    }


    /**
     * Wraps and returns the provided handler in a context handler with the relative path set, which in turn is
     * wrapped by a basic auth security handler bound by the single role admin.
     * @param path the relative path to which the context should be bound to.
     * @param handler An already initialized handler which is to be wrapped
     */
    static Handler getSecureHandler(String path, Handler handler) {
        var context = new ContextHandler();
        context.setContextPath(path);
        context.setHandler(handler);
        var security = new ConstraintSecurityHandler();
        security.setAuthenticator(new BasicAuthenticator());

        var constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(SecurityManager.getInstance().getRoles());
        var mapping = new ConstraintMapping();
        mapping.setPathSpec(path);
        mapping.setConstraint(constraint);
        security.setConstraintMappings(Collections.singletonList(mapping));

        var loginService = SecurityManager.getInstance().getAdminLoginService();
        security.setLoginService(loginService);
        security.setHandler(context);
        return security;
    }

    /**
     * Initiates and returns a collection of enpoints which have yet to be registered with any server
     * @return A collection of handlers with paths preregistered
     */
    static ContextHandlerCollection getEndpointsHandler() {
        var endpoints = new ContextHandlerCollection();
        endpoints.addHandler(getContextHandler("/webhook", new WebhookHandler(environment, storage)));
        endpoints.addHandler(getSecureHandler("/build/all", new BuildAllHandler(storage)));
        endpoints.addHandler(getSecureHandler("/build", new BuildHandler(storage)));
        return endpoints;
    }

    /**
     * Start the server, simple as.
     * @param args the port number can be specified in args. If none is given, DEFAULT_PORT_NUMBER is used.
     * @throws Exception if the server is interrupted.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting up server...");
        var portNumber = getPortNumberFromInputOrElseDefault(args);
        var server = new Server(portNumber);
        server.setHandler(getEndpointsHandler());

        var loginService = SecurityManager.getInstance().getAdminLoginService();
        server.addBean(loginService);

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
