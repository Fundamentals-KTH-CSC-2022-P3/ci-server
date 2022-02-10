package fundamentals.server;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import java.util.Collections;


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

    static Handler getContextHandler(String path, Handler handler) {
        var context = new ContextHandler();
        context.setContextPath(path);
        context.setHandler(handler);
        return context;
    }

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

    static ContextHandlerCollection getEndpointsHandler() {
        var endpoints = new ContextHandlerCollection();
        endpoints.addHandler(getContextHandler("/webhook", new WebhookHandler(environment, storage)));
        endpoints.addHandler(getSecureHandler("/build/all", new BuildAllHandler(storage)));
        endpoints.addHandler(getSecureHandler("/build", new BuildHandler(storage)));
        return endpoints;
    }

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
