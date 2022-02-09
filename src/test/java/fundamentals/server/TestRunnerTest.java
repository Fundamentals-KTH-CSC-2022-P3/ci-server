package fundamentals.server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRunnerTest {
    static final String relativeReportPath = "target/surefire-reports";

    static RepoManager repoManager;
    static File reportDir;
    static File report;

    @BeforeAll
    static void setup() throws IOException {
        repoManager = new RepoManager();
        reportDir = new File(repoManager.repoDir, relativeReportPath);
        if (!reportDir.mkdirs()) {
            throw new IOException("Could not create Surefire reports directory");
        }
        report = new File(reportDir, "report.txt");
        if (!report.createNewFile()) {
            throw new IOException("Could not create Surefire reports directory");
        }
    }

    @AfterAll
    static void tearDown() {
        repoManager.cleanUp();
    }

    @Test
    void failingReportResultsInFail() throws IOException {
        FileWriter fw = new FileWriter(report);
        String failingReportContent = "Tests run: 2, Failures: 1, etc";
        fw.write(failingReportContent);
        fw.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        TestRunner testRunner = new TestRunner(repoManager);
        testRunner.run();

        String output = outputStream.toString();
        boolean failed = Arrays.asList(output
                .split("(\\r\\n|\\r)"))
                .contains("Tests failed!");
        assertTrue(failed);
    }

    @Test
    void passingReportResultsInPass() throws IOException {
        FileWriter fw = new FileWriter(report);
        String passingReportContent = "Tests run: 2, Failures: 0, etc";
        fw.write(passingReportContent);
        fw.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        TestRunner testRunner = new TestRunner(repoManager);
        testRunner.run();

        String output = outputStream.toString();
        boolean passed = Arrays.asList(output
                        .split("(\\r\\n|\\r)"))
                .contains("Tests were successful!");
        assertTrue(passed);
    }
}
