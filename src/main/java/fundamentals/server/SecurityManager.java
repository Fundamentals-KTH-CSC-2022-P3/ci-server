package fundamentals.server;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Credential;

public class SecurityManager {

    public static final String ADMIN_DEFAULT_VALUE = "admin";
    public static final String ADMIN_PASSWORD_DEFAULT = "adminpass";
    private final static SecurityManager instance = new SecurityManager();
    private final static String usernameEnvVariableName = "CI_SERVER_USER";
    private final static String passwordEnvVariableName = "CI_SERVER_PASS";
    private final static String[] ROLES = new String[]{"admin"};
    private String username;
    private Credential password;
    private HashLoginService adminLoginService = null;

    private SecurityManager() {
        loadUsername();
        loadPassword();
    }

    private void loadUsername(){
        this.username = Environment.loadEnvironmentVariableOrElse(usernameEnvVariableName, ADMIN_DEFAULT_VALUE);
    }

    private void loadPassword(){
        var password = Environment.loadEnvironmentVariableOrElse(passwordEnvVariableName, ADMIN_PASSWORD_DEFAULT);
        this.password = Credential.getCredential(password);
    }

    public Credential getAdminCredential() {
        return password;
    }

    public String getAdminUsername(){
        return username;
    }

    public static SecurityManager getInstance() {
        return instance;
    }

    public LoginService getAdminLoginService() {
        if (adminLoginService != null)
            return adminLoginService;
        adminLoginService = new HashLoginService();
        var store = new UserStore();
        var credential = SecurityManager.getInstance().getAdminCredential();
        var admin = SecurityManager.getInstance().getAdminUsername();
        store.addUser(admin, credential, ROLES);
        adminLoginService.setUserStore(store);
        return adminLoginService;
    }

    public String[] getRoles() {
        // Returning clone so that no caller can change overall roles
        return ROLES.clone();
    }
}
