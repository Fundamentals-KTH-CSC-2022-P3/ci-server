package fundamentals.server.gitTooling;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Contains methods that can be called to work with the Github commit API.
 */
public class GithubCommitAPI {

    /**
     * The root url for the github api
     */
    public static final String GITHUB_API_ROOT_URL = "https://api.github.com";

    /**
     * The four different statuses that can be assigned to each Github commit.
     */
    public enum CommitStatus {
        ERROR("error"),
        FAILURE("failure"),
        PENDING("pending"),
        SUCCESS("success");

        final String name;

        private CommitStatus(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final String owner;
    private final String repository;
    private final String commitHash;
    private final String username;
    private final String personalAccessToken;

    /**
     * Creates a new {@code GithubCommitAPI} object against a specific owner, repository and commit.
     * To gain access to this repository we will need a username and a personal access token.
     *
     * @param owner               the owner of the repository that we want to work with.
     * @param repository          the repository we want to work with.
     * @param commitHash          the commit we want to work with, specify the SHA hash.
     * @param username            the username with access to the repository.
     * @param personalAccessToken the personal access token for the user.
     */
    public GithubCommitAPI(String owner, String repository, String commitHash, String username, String personalAccessToken) {
        this.owner = owner;
        this.repository = repository;
        this.commitHash = commitHash;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
    }

    /**
     * Generates a request to set the commit status of a specific commit to error.
     *
     * @param description a description for the commit status. For example "all tests passed".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return a {@code GithubCommitAPIRequest} object that can be used to send the request.
     */
    public GithubCommitAPIRequest setCommitStatusError(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.ERROR, description, targetUrl);
    }

    /**
     * Generates a request to set the commit status of a specific commit to error.
     *
     * @param description a description for the commit status. For example "one test failed".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return a {@code GithubCommitAPIRequest} object that can be used to send the request.
     */
    public GithubCommitAPIRequest setCommitStatusFailure(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.FAILURE, description, targetUrl);
    }

    /**
     * Generates a request to set the commit status of a specific commit to error.
     *
     * @param description a description for the commit status. For example "running tests...".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return a {@code GithubCommitAPIRequest} object that can be used to send the request.
     */
    public GithubCommitAPIRequest setCommitStatusPending(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.PENDING, description, targetUrl);
    }

    /**
     * Generates a request to set the commit status of a specific commit to error.
     *
     * @param description a description for the commit status. For example "all tests succeeded".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return a {@code GithubCommitAPIRequest} object that can be used to send the request.
     */
    public GithubCommitAPIRequest setCommitStatusSuccess(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.SUCCESS, description, targetUrl);
    }

    // For how a commit status is created see: https://docs.github.com/en/rest/reference/commits
    private GithubCommitAPIRequest setCommitStatus(CommitStatus status, String description, String targetUrl) {
        try {
            URL url = buildGithubURL("repos", owner, repository, "statuses", commitHash);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            setBasicAuthorizationHeader(http);
            setContentTypeHeader(http, "application/json; charset=UTF-8");
            setAcceptHeader(http, "application/vnd.github.v3+json");
            byte[] body = getCommitStatusJSONObject(status, description, targetUrl);
            http.setFixedLengthStreamingMode(body.length);
            return new GithubCommitAPIRequest(http, body);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Returns the JSON object that should be stored in the HTTP body of the "create a commit status" request.
    private byte[] getCommitStatusJSONObject(CommitStatus status, String description, String targetUrl) {
        JSONObject object = new JSONObject();
        object.put("state", status.toString());
        object.put("description", description);
        object.put("target_url", targetUrl);
        object.put("context", username);
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Will build a URL object from Github's API root.
    // For example the call buildGithubURL("repo", "example")
    // returns a URL object with the path "https://api.github.com/repo/example"
    private URL buildGithubURL(String... parts) throws MalformedURLException {
        StringBuilder url = new StringBuilder(GITHUB_API_ROOT_URL);
        for (String part : parts) {
            url.append("/");
            url.append(part);
        }
        return new URL(url.toString());
    }

    /**
     * @return Returns the base64 string of the concatenation of the username, colon (':') and the personal access token.
     */
    public String getBasicAuthorizationCredentials() {
        return Base64.getEncoder().encodeToString((username + ":" + personalAccessToken).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Update the HTTP header to enable basic authorization for the CI server.
     *
     * @param http
     */
    private void setBasicAuthorizationHeader(HttpURLConnection http) {
        http.setRequestProperty("Authorization", "Basic " + getBasicAuthorizationCredentials());
    }

    /**
     * Set the Content-Type HTTP header.
     *
     * @param http
     * @param contentType
     */
    private void setContentTypeHeader(HttpURLConnection http, String contentType) {
        http.setRequestProperty("Content-Type", contentType);
    }

    /**
     * Set the Accept HTTP header.
     *
     * @param http
     * @param accept
     */
    private void setAcceptHeader(HttpURLConnection http, String accept) {
        http.setRequestProperty("Accept", accept);
    }
}
