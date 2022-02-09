package fundamentals.server;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This unit tests the compiler by cloning a remote repo and trying to compile it.
 * A better way would be to create a minimal Maven repo as part of the test fixture
 * since that would remove an external dependency. //Arvid
 */
public class CompilerTest {
    private static final String validPayload = "{\"ref\": \"refs/heads/main\",\"repository\": {\"name\": \"ci-server\",\"clone_url\": \"https://github.com/Fundamentals-KTH-CSC-2022-P3/ci-server.git\",}}\n";
    private RepoManager repoManager;
    private File workDir = new File("testWorkDir/");
    private Compiler compiler;

    @BeforeEach
    void setup() throws IOException {
        if (!workDir.mkdirs()) {
            throw new IOException("Could not create directory " + workDir.getAbsolutePath());
        }
        repoManager = new RepoManager(validPayload, workDir);
        repoManager.cloneRepo();
        compiler = new Compiler(repoManager);
    }

    @AfterEach
    void tearDown() {
        RepoManager.deleteDirectory(workDir);
    }

    @Test
    void compileClonedDirectoryIsSuccessful() throws IOException, InterruptedException {
        assertEquals(compiler.compile(), 0);
    }

    @Test
    void compileDisturbedDirectoryFails() throws IOException, InterruptedException {
        File pom = new File(repoManager.repoDir, "pom.xml");
        FileWriter fw = new FileWriter(pom);
        fw.append("invalid XML");
        fw.close();
        assertNotEquals(compiler.compile(), 0);
    }
}
