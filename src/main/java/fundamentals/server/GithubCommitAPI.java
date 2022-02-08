package fundamentals.server;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Contains methods that can be called to work with the Github API for a specific owner and repository.
 */
public class GithubCommitAPI {

    public static final String GITHUB_API_ROOT_URL = "https://api.github.com";

    // Should get from the environment (fix later).
    public static final String CI_USERNAME = "dd2480-ci-user";
    public static final String CI_PERSONAL_ACCESS_TOKEN = "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";

    // The credentials for basic authorization.
    public static final String CREDENTIALS = Base64.getEncoder().encodeToString((CI_USERNAME + ":" + CI_PERSONAL_ACCESS_TOKEN).getBytes(StandardCharsets.UTF_8));

    private final String owner;
    private final String repository;
    private final String commitHash;

    /**
     * Creates a new {@code GithubCommitAPI} object against a specific owner, repository and commit.
     *
     * @param owner      the owner of the repository that we want to work with.
     * @param repository the repository we want to work with.
     * @param commitHash the commit we want to work with, specify the SHA hash.
     */
    public GithubCommitAPI(String owner, String repository, String commitHash) {
        this.owner = owner;
        this.repository = repository;
        this.commitHash = commitHash;
    }

    /**
     * Set the commit status of a specific commit to error.
     *
     * @param description a description for the commit status. For example "all tests passed".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return true if the commit status was set, otherwise false.
     */
    public boolean setCommitStatusError(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.ERROR, description, targetUrl);
    }

    /**
     * Set the commit status of a specific commit to failure.
     *
     * @param description a description for the commit status. For example "one test failed".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return true if the commit status was set, otherwise false.
     */
    public boolean setCommitStatusFailure(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.FAILURE, description, targetUrl);
    }

    /**
     * Set the commit status of a specific commit to pending.
     *
     * @param description a description for the commit status. For example "running tests...".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return true if the commit status was set, otherwise false.
     */
    public boolean setCommitStatusPending(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.PENDING, description, targetUrl);
    }

    /**
     * Set the commit status of a specific commit to success.
     *
     * @param description a description for the commit status. For example "all tests succeeded".
     * @param targetUrl   a target url that will be shown together with the commit status (this is often a URL to a page that shows more build information).
     * @return true if the commit status was set, otherwise false.
     */
    public boolean setCommitStatusSuccess(String description, String targetUrl) {
        return setCommitStatus(CommitStatus.SUCCESS, description, targetUrl);
    }

    // For how a commit status is created see: https://docs.github.com/en/rest/reference/commits
    private boolean setCommitStatus(CommitStatus status, String description, String targetUrl) {
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
            http.connect();
            http.getOutputStream().write(body);

            // If the commit status of the commit has been updated we retrieve the HTTP response code 201 (Created).
            return http.getResponseCode() == 201;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Returns the JSON object that should be stored in the HTTP body of the "create a commit status" request.
    private byte[] getCommitStatusJSONObject(CommitStatus status, String description, String targetUrl) {
        JSONObject object = new JSONObject();
        object.put("state", status.toString());
        object.put("description", description);
        object.put("target_url", targetUrl);
        object.put("context", CI_USERNAME);
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

    // Update the HTTP header to enable basic authorization for the CI server.
    private void setBasicAuthorizationHeader(HttpURLConnection http) {
        http.setRequestProperty("Authorization", "Basic " + CREDENTIALS);
    }

    // Set the Content-Type HTTP header.
    private void setContentTypeHeader(HttpURLConnection http, String contentType) {
        http.setRequestProperty("Content-Type", contentType);
    }

    // Set the Accept HTTP header.
    private void setAcceptHeader(HttpURLConnection http, String accept) {
        http.setRequestProperty("Accept", accept);
    }
}
