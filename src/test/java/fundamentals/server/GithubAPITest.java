package fundamentals.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the Github API against a test repository.
 */
public class GithubAPITest {

    private static final String OWNER = "Fundamentals-KTH-CSC-2022-P3";
    private static final String REPOSITORY = "ci-server";
    private static final String COMMIT_SHA = "323";

    private GithubAPI api = new GithubAPI(OWNER, REPOSITORY);

    /**
     * Ensure that it is possible to set the commit status to error.
     */
    @Test
    void setCommitStatusToErrorTest() {
        assertTrue(api.setCommitStatusError(COMMIT_SHA, "", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to failure.
     */
    @Test
    void setCommitStatusToFailureTest() {
        assertTrue(api.setCommitStatusFailure(COMMIT_SHA, "", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to pending.
     */
    @Test
    void setCommitStatusToPendingTest() {
        assertTrue(api.setCommitStatusPending(COMMIT_SHA, "", ""));
    }

    /**
     * Ensure that it is possible to set the commit status to success.
     */
    @Test
    void setCommitStatusToSuccessTest() {
        assertTrue(api.setCommitStatusSuccess(COMMIT_SHA, "", ""));
    }
}
