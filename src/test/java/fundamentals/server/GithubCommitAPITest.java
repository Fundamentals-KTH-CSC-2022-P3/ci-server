package fundamentals.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the Github API against a test repository.
 */
public class GithubCommitAPITest {

    private static final String OWNER = "example-owner";
    private static final String REPOSITORY = "example-repo";
    private static final String COMMIT_HASH = "example-hash";
    private static final String USERNAME = "example-username";
    private static final String TOKEN = "example-token";

    private GithubCommitAPI api = new GithubCommitAPI(OWNER, REPOSITORY, COMMIT_HASH, USERNAME, TOKEN);

    /**
     * Ensure that the URL path is correct it should be: /repos/{owner}/{repo}/statuses/{sha}
     */
    @Test
    @DisplayName("URL path test for the create commit status request")
    void urlPathTest() {
        GithubCommitAPIRequest request = api.setCommitStatusError("example-description", "example-target-url");
        assertEquals("/repos/" + OWNER + "/" + REPOSITORY + "/statuses/" + COMMIT_HASH, request.getURLPath());
    }

    /**
     * Ensure that the HTTP request is a POST request.
     */
    @Test
    @DisplayName("HTTP method test for the create commit status request")
    void postRequestTest() {
        GithubCommitAPIRequest request = api.setCommitStatusError("example-description", "example-target-url");
        assertEquals("POST", request.getHTTPMethod());
    }

    /**
     * Ensure that the HTTP request has the correct Content-Type header: "application/json; charset=UTF-8".
     */
    @Test
    @DisplayName("Content-Type header test for the create commit status request")
    void contentTypeHeaderTest() {
        GithubCommitAPIRequest request = api.setCommitStatusError("example-description", "example-target-url");
        assertEquals("application/json; charset=UTF-8", request.getHTTPHeaderField("Content-Type"));
    }

    /**
     * Ensure that the HTTP request has the correct Accept header: "application/vnd.github.v3+json".
     */
    @Test
    @DisplayName("Accept header test for the create commit status request")
    void acceptHeaderTest() {
        GithubCommitAPIRequest request = api.setCommitStatusError("example-description", "example-target-url");
        assertEquals("application/vnd.github.v3+json", request.getHTTPHeaderField("Accept"));
    }

    /**
     * Ensure that the HTTP request has the correct Authorization credentials: base64(USERNAME + ":" + TOKEN).
     */
    @Test
    @DisplayName("Basic authorization credentials test for the create commit status request")
    void basicAuthorizationCredentialsTest() {
        String credentials = Base64.getEncoder().encodeToString((USERNAME + ":" + TOKEN).getBytes(StandardCharsets.UTF_8));
        assertEquals(credentials, api.getBasicAuthorizationCredentials());
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "state": "error".
     */
    @Test
    @DisplayName("Error state test for the create commit status request")
    void errorStateTest() {
        GithubCommitAPIRequest request = api.setCommitStatusError("example-description", "example-target-url");
        assertEquals("error", request.getHTTPBodyAsJSON().getString("state"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "state": "failure".
     */
    @Test
    @DisplayName("Failure state test for the create commit status request")
    void failureStateTest() {
        GithubCommitAPIRequest request = api.setCommitStatusFailure("example-description", "example-target-url");
        assertEquals("failure", request.getHTTPBodyAsJSON().getString("state"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "state": "pending".
     */
    @Test
    @DisplayName("Pending state test for the create commit status request")
    void pendingStateTest() {
        GithubCommitAPIRequest request = api.setCommitStatusPending("example-description", "example-target-url");
        assertEquals("pending", request.getHTTPBodyAsJSON().getString("state"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "state": "success".
     */
    @Test
    @DisplayName("Success state test for the create commit status request")
    void successStateTest() {
        GithubCommitAPIRequest request = api.setCommitStatusSuccess("example-description", "example-target-url");
        assertEquals("success", request.getHTTPBodyAsJSON().getString("state"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "description": "example-description".
     */
    @Test
    @DisplayName("Description test for the create commit status request")
    void descriptionTest() {
        GithubCommitAPIRequest request = api.setCommitStatusSuccess("example-description", "example-target-url");
        assertEquals("example-description", request.getHTTPBodyAsJSON().getString("description"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "target_url": "example-target-url".
     */
    @Test
    @DisplayName("Target URL test for the create commit status request")
    void targetUrlTest() {
        GithubCommitAPIRequest request = api.setCommitStatusSuccess("example-description", "example-target-url");
        assertEquals("example-target-url", request.getHTTPBodyAsJSON().getString("target_url"));
    }

    /**
     * Ensure that the HTTP request has the correct JSON object in the body.
     * Check that the JSON object contains the following key-value pair: "context": USERNAME.
     */
    @Test
    @DisplayName("Context test for the create commit status request")
    void contextTest() {
        GithubCommitAPIRequest request = api.setCommitStatusSuccess("example-description", "example-target-url");
        assertEquals(USERNAME, request.getHTTPBodyAsJSON().getString("context"));
    }

}
