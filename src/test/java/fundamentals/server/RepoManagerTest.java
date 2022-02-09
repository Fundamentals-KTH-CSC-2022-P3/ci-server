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
    private static final String validPayload = "{\"ref\": \"refs/heads/main\",\"repository\": {\"name\": \"Hello-World\",\"clone_url\": \"https://github.com/Codertocat/Hello-World.git\",}}\n";
    private static final String invalidPayload = "{}";
    private static File workDir;
    private static RepoManager repoManager;

    @BeforeAll
    static void setup() throws IOException {
        workDir = new File("testWorkDir/");
        if (workDir.exists())
            assertTrue(workDir.mkdir());

        repoManager = new RepoManager(validPayload, workDir);
    }

    @AfterAll
    static void tearDown() {
        RepoManager.deleteDirectory(workDir);
    }

    @Test
    void repoManagerWithInvalidPayloadThrowsException() throws IOException {
        assertThrows(JSONException.class, () -> new RepoManager(invalidPayload, workDir));
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


}
