package fundamentals.server;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Credential;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for managing credentials and security of the server.
 */
public class SecurityManager {

    private static final String ADMIN_DEFAULT_VALUE = "admin";
    private static final String ADMIN_PASSWORD_DEFAULT = "adminpass";
    private final static SecurityManager instance = new SecurityManager();
    private final static String usernameEnvVariableName = "CI_SERVER_USER";
    private final static String passwordEnvVariableName = "CI_SERVER_PASS";
    private final static String[] ROLES = new String[]{"admin"};
    private String username;
    private Credential password;
    private HashLoginService adminLoginService = null;
    private List<URI> whitelist = new ArrayList<>();

    private SecurityManager() {
        loadUsername();
        loadPassword();
        try {
            whitelist.add(URI.create("https://github.com/Fundamentals-KTH-CSC-2022-P3/ci-server"));
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to whitelist project repository");
        }
    }

    private void loadUsername(){
        this.username = Environment.loadEnvironmentVariableOrElse(usernameEnvVariableName, ADMIN_DEFAULT_VALUE);
    }

    private void loadPassword(){
        var password = Environment.loadEnvironmentVariableOrElse(passwordEnvVariableName, ADMIN_PASSWORD_DEFAULT);
        this.password = Credential.getCredential(password);
    }

    private Credential getAdminCredential() {
        return password;
    }

    private String getAdminUsername(){
        return username;
    }

    public static SecurityManager getInstance() {
        return instance;
    }

    /**
     * Retrieves the LoginService which gives the admin user total access.
     * @return The singular admin LoginService used by the server.
     */
    public LoginService getAdminLoginService() {
        if (adminLoginService != null)
            return adminLoginService;
        adminLoginService = new HashLoginService();
        var store = new UserStore();
        var credential = getAdminCredential();
        var admin = getAdminUsername();
        store.addUser(admin, credential, ROLES);
        adminLoginService.setUserStore(store);
        return adminLoginService;
    }

    /**
     * Check that the URI of a repository is whitelisted.
     * @param repository the URI to check against the whitelist
     * @throws SecurityException if repository is not whitelisted.
     */
    public void verifyAgainstWhitelist(URI repository) {
        if(whitelist.contains(repository))
            return;
        throw new SecurityException("Repository " +repository+ " not whitelisted");
    }

    /**
     * Wraps and returns the provided handler in a context handler with a set path
     * @return All roles supported by the current server
     */
    public String[] getRoles() {
        // Returning clone so that no caller can change overall roles
        return ROLES.clone();
    }
}
