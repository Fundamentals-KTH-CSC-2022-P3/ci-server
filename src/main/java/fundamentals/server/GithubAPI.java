package fundamentals.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GithubAPI {

    public static final String GITHUB_API_ROOT_URL = "https://api.github.com";

    // Should get from the environment (fix later).
    public static final String CI_USERNAME = "dd2480-ci-user";
    public static final String CI_PERSONAL_ACCESS_TOKEN = "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";

    // The credentials for basic authorization.
    public static final String CREDENTIALS = Base64.getEncoder().encodeToString((CI_USERNAME + ":" + CI_PERSONAL_ACCESS_TOKEN).getBytes(StandardCharsets.UTF_8));

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

    public GithubAPI(String owner, String repository) {
        this.owner = owner;
        this.repository = repository;
    }

    public boolean setCommitStatusError(String sha, String description, String targetUrl) {
        return setCommitStatus(CommitStatus.ERROR, sha, description, targetUrl);
    }

    public boolean setCommitStatusFailure(String sha, String description, String targetUrl) {
        return setCommitStatus(CommitStatus.FAILURE, sha, description, targetUrl);
    }

    public boolean setCommitStatusPending(String sha, String description, String targetUrl) {
        return setCommitStatus(CommitStatus.PENDING, sha, description, targetUrl);
    }

    public boolean setCommitStatusSuccess(String sha, String description, String targetUrl) {
        return setCommitStatus(CommitStatus.SUCCESS, sha, description, targetUrl);
    }

    private boolean setCommitStatus(CommitStatus status, String sha, String description, String targetUrl) {
        try {
            URL url = buildGithubURL("repos", owner, repository, "statuses", sha);
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

            // The commit status of the commit has been updated if we retrieve the HTTP response code 201 (Created).
            return http.getResponseCode() == 201;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private byte[] getCommitStatusJSONObject(CommitStatus status, String description, String targetUrl) {
        StringBuilder jsonObject = new StringBuilder();
        jsonObject.append("{");
        jsonObject.append("\"" + "state" + "\"" + ":");
        jsonObject.append("\"" + status + "\"" + ",");
        jsonObject.append("\"" + "description" + "\"" + ":");
        jsonObject.append("\"" + description + "\"" + ",");
        jsonObject.append("\"" + "target_url" + "\"" + ":");
        jsonObject.append("\"" + targetUrl + "\"" + ",");
        jsonObject.append("\"" + "context" + "\"" + ":");
        jsonObject.append("\"" + CI_USERNAME + "\"");
        jsonObject.append("}");
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    private URL buildGithubURL(String... parts) throws MalformedURLException {
        StringBuilder url = new StringBuilder(GITHUB_API_ROOT_URL);
        for (String part : parts) {
            url.append("/");
            url.append(part);
        }
        return new URL(url.toString());
    }

    private void setBasicAuthorizationHeader(HttpURLConnection http) {
        http.setRequestProperty("Authorization", "Basic " + CREDENTIALS);
    }

    private void setContentTypeHeader(HttpURLConnection http, String contentType) {
        http.setRequestProperty("Content-Type", contentType);
    }

    private void setAcceptHeader(HttpURLConnection http, String accept) {
        http.setRequestProperty("Accept", accept);
    }
}
