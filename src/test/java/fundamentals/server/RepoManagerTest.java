package fundamentals.server;

import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

public class RepoManagerTest {
    private static final String invalidPayload = "{}";
    private static RepoManager repoManager;

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        repoManager = new RepoManager();
    }

    @AfterAll
    static void tearDown() {
        RepoManager.deleteDirectory(repoManager.workDir);
    }

    @Test
    void repoManagerWithInvalidPayloadThrowsException() {
        assertThrows(JSONException.class, () -> new RepoManager(invalidPayload));
    }

    @Test
    void repoManagerCreatesWorkingDirectory() {
        assertTrue(repoManager.parentDir.exists());
    }

    @Test
    void cloneRepoCreatesLocalCopy() {
        File gitFolder = new File(repoManager.repoDir, ".git");
        assertTrue(gitFolder.exists());
    }

    @Test
    void checkoutChangesBranch() throws InterruptedException, IOException {
        String newBranchName = "name-of-branch-to-be-created-that-does-not-already-exist";
        String[] checkExistingBranchesCmd = new String[]{"git", "branch"};
        Process branchProcess = Runtime.getRuntime().exec(checkExistingBranchesCmd, null, repoManager.workDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(branchProcess.getInputStream()));
        branchProcess.waitFor();
        assertFalse(reader.lines().anyMatch(line -> line.matches("\\.*" + newBranchName + "\\.*")));

        String[] createBranchAndCheckoutCmd = new String[]{"git", "checkout", "-b", newBranchName};
        Process checkoutProcess = Runtime.getRuntime().exec(createBranchAndCheckoutCmd, null, repoManager.repoDir);
        checkoutProcess.waitFor();

        branchProcess = Runtime.getRuntime().exec(checkExistingBranchesCmd, null, repoManager.repoDir);
        reader = new BufferedReader(new InputStreamReader(branchProcess.getInputStream()));
        String selectedBranchPattern = "\\* " + newBranchName;

        assertTrue(reader.lines().anyMatch(line -> line.matches(selectedBranchPattern)));
    }
}
