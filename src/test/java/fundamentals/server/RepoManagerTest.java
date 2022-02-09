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
    private static final String validPayload = "{\"ref\": \"refs/heads/main\",\"repository\": {\"name\": \"ci-server\",\"clone_url\": \"https://github.com/Fundamentals-KTH-CSC-2022-P3/ci-server.git\",}}\n";
    private static final String invalidPayload = "{}";
    private static RepoManager repoManager;

    @BeforeAll
    static void setup() throws IOException {
        repoManager = new RepoManager(validPayload);
    }

    @AfterAll
    static void tearDown() {
        repoManager.cleanUp();
    }

    @Test
    void repoManagerWithInvalidPayloadThrowsException() throws IOException {
        assertThrows(JSONException.class, () -> new RepoManager(invalidPayload));
    }

    @Test
    void repoManagerCreatesWorkingDirectory() {
        assertTrue(repoManager.parentDir.exists());
    }

    @Test
    void cloneRepoCreatesLocalCopy() {
        repoManager.cloneRepo();
        File gitFolder = new File(repoManager.repoDir, ".git");
        assertTrue(gitFolder.exists());
    }

    @Test
    void checkoutChangesBranch() throws InterruptedException, IOException {
        repoManager.cloneRepo();
        String newBranchName = "name-of-branch-to-be-created-that-does-not-already-exist";
        String[] checkExistingBranchesCmd = new String[]{"git", "branch"};
        Process branchProcess = Runtime.getRuntime().exec(checkExistingBranchesCmd, null, repoManager.repoDir);
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
