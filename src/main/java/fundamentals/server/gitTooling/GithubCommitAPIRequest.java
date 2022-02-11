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

    /**
     * Get the path from the URL in the http request sent to github
     *
     * @return
     */
    public String getURLPath() {
        return http.getURL().getPath();
    }

    /**
     * Get the method (POST|GET|...)
     *
     * @return
     */
    public String getHTTPMethod() {
        return http.getRequestMethod();
    }

    /**
     * Get HTTP a http header
     *
     * @param name name of the header
     * @return
     */
    public String getHTTPHeaderField(String name) {
        return http.getRequestProperty(name);
    }

    /**
     * Get the body of the http request
     *
     * @return
     */
    public byte[] getHTTPBody() {
        return body;
    }

    /**
     * Get the http body as json
     *
     * @return
     */
    public JSONObject getHTTPBodyAsJSON() {
        return new JSONObject(new String(body, StandardCharsets.UTF_8));
    }
}
