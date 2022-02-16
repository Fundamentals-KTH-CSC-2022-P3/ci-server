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

    /**
     * Admin default username and password to be used if nothing else is provided
     */
    private static final String ADMIN_DEFAULT_VALUE = "admin";
    private static final String ADMIN_PASSWORD_DEFAULT = "adminpass";

    /**
     * The only role provided is the admin role
     */
    private final static String[] ROLES = new String[]{"admin"};


    /**
     * The environment variables where the admin login details are provided
     */
    private final static String usernameEnvVariableName = "CI_SERVER_USER";
    private final static String passwordEnvVariableName = "CI_SERVER_PASS";

    private final static SecurityManager instance = new SecurityManager();

    /**
     * Admin is the only available user, username and password saved as a local variables
     */
    private String username;
    private Credential password;

    private HashLoginService adminLoginService = null;

    /**
     * Whitelist over the repositories allowed to be run on this server
     */
    private List<URI> RepositoryWhitelist = new ArrayList<>();

    private SecurityManager() {
        loadUsername();
        loadPassword();
        try {
            RepositoryWhitelist.add(URI.create("https://github.com/Fundamentals-KTH-CSC-2022-P3/ci-server"));
        } catch (IllegalArgumentException ex) {
            System.err.println("Failed to whitelist project repository");
        }
    }

    /**
     * Loads the admin username from the environment variable
     */
    private void loadUsername(){
        this.username = Environment.loadEnvironmentVariableOrElse(usernameEnvVariableName, ADMIN_DEFAULT_VALUE);
    }

    /**
     * Loads the admin password from the environment variable
     */
    private void loadPassword(){
        var password = Environment.loadEnvironmentVariableOrElse(passwordEnvVariableName, ADMIN_PASSWORD_DEFAULT);
        this.password = Credential.getCredential(password);
    }

    /**
     * Retrieves the admin password wrapped in a secure class
     * @return the hashed password for the admin user
     */
    private Credential getAdminCredential() {
        return password;
    }

    /**
     * Retrieve the admin username
     * @return admin username
     */
    private String getAdminUsername(){
        return username;
    }

    /**
     * Retrieves the only instance of the SecurityManager on the server
     * @return the static security manager
     */
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
        if(RepositoryWhitelist.contains(repository))
            return;
        throw new SecurityException("Repository " +repository+ " not whitelisted");
    }

    /**
     * Gets all roles used by the system. The returned roles are copies of the real roles, and thus changing this
     * value will not affect the supported roles
     * @return All roles supported by the current server
     */
    public String[] getRoles() {
        // Returning clone so that no caller can change overall roles
        return ROLES.clone();
    }
}
