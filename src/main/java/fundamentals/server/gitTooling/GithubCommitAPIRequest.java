package fundamentals.server.gitTooling;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Represents an HTTP request to create a commit status for a commit on Github.
 */
public class GithubCommitAPIRequest {

    private HttpURLConnection http;
    private byte[] body;

    /**
     * Creates a new HTTP request that can be sent to Github to create a commit status.
     *
     * @param http an {@code HttpURLConnection} object that stores the whole request (but not the body).
     * @param body the body with should be a JSON object encoded as a UTF-8 byte sequence.
     */
    public GithubCommitAPIRequest(HttpURLConnection http, byte[] body) {
        this.http = http;
        this.body = body;
    }

    /**
     * Will send the request to Github.
     *
     * @return true if the commit status was created, otherwise false.
     */
    public boolean send() {
        try {
            http.connect();
            http.getOutputStream().write(body);
            // If the commit status of the commit has been updated we retrieve the HTTP response code 201 (Created).
            return http.getResponseCode() == 201;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // The methods below are only used for unit-testing.

    public String getURLPath() {
        return http.getURL().getPath();
    }

    public String getHTTPMethod() {
        return http.getRequestMethod();
    }

    public String getHTTPHeaderField(String name) {
        return http.getRequestProperty(name);
    }

    public byte[] getHTTPBody() {
        return body;
    }

    public JSONObject getHTTPBodyAsJSON() {
        return new JSONObject(new String(body, StandardCharsets.UTF_8));
    }
}
