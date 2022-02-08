package fundamentals.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the Github API against a test repository.
 */
public class GithubCommitAPITest {

    private static final String OWNER = "Fundamentals-KTH-CSC-2022-P3";
    private static final String REPOSITORY = "ci-server";
    private static final String COMMIT_HASH = "8d3fc71c8ab65bfc50d6822fb163408250978d61";

    private GithubCommitAPI api = new GithubCommitAPI(OWNER, REPOSITORY, COMMIT_HASH);

    /**
     * Ensure that it is possible to set the commit status to error.
     */
    @Test
    void setCommitStatusToErrorTest() {
        assertTrue(api.setCommitStatusError("", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to failure.
     */
    @Test
    void setCommitStatusToFailureTest() {
        assertTrue(api.setCommitStatusFailure("", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to pending.
     */
    @Test
    void setCommitStatusToPendingTest() {
        assertTrue(api.setCommitStatusPending("", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to success.
     */
    @Test
    void setCommitStatusToSuccessTest() {
        assertTrue(api.setCommitStatusSuccess("", ""));
    }
}
