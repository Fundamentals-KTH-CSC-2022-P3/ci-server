package fundamentals.server;
import org.json.*;

import java.io.*;

/**
 * Takes the POST:ed payload string from a GitHub push webhook and clones the repository specified in the payload.
 * Then, "mvn test" is run inside the repository, and if any test fails this will be noted on standard output.
 * If all tests are successful, this is printed on standard output.
 */
public class TestRunner {
    private final RepoManager repoManager;
    private final Runtime runtime = Runtime.getRuntime();

    /**
     * Takes a RepoManager that is connected to the repo to test, and tests the repo, reporting any
     * failures.
     * @param repoManager the payload POST:ed by a GitHub webhook
     */
    public TestRunner(RepoManager repoManager) {
        this.repoManager = repoManager;
    }

    public void run() {
        runTests();
        checkTestResults();
    }

    private void runTests() {
        String[] mavenCmd = {"mvn.cmd", "test"};
        System.out.println("running maven");
        try {
            runtime.exec(mavenCmd, null, repoManager.repoDir).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for mvn test!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Could not run maven test in " + repoManager.repoDir.getAbsolutePath());
            ioException.printStackTrace();
        }
    }

    void checkTestResults() {
        File testReportDirectory = new File(repoManager.repoDir, "target/surefire-reports");
        var files = testReportDirectory.listFiles((File f) -> f.getName().matches(".+\\.txt"));
        for (var file : files) {
            if (!testsInReportWereSuccessful(file)) {
                // TODO: set status of remote repository to success
                System.out.println("Tests failed!");
                return;
            }
        }
        // TODO: set status of remote repository to fail
        System.out.println("Tests were successful!");
    }

    private boolean testsInReportWereSuccessful(File report) {
        // NOTE: this could be made more robust by parsing the XML file instead. I opted for the
        // quick and dirty way of looking for the string "Failure: 0" in the human readable report for now.

        // NOTE: We might want to gather more data about the tests, right now we get a boolean "Pass/Fail"
        // and nothing more.
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(report));
        } catch (FileNotFoundException fileNotFoundException) {
            // This should never happen since we only call this method on files that we get from `listFiles`
            System.err.println("File " + report.getAbsolutePath() + " was not found.");
            return false;
        }
        boolean result = reader.lines().anyMatch(s -> s.matches("Tests run: \\d+, Failures: 0,.+"));
        try {
            reader.close();
        } catch (IOException ioException) {
            System.err.println("Could not close file " + report.getAbsolutePath());
            ioException.printStackTrace();
        }
        return result;
    }
}
