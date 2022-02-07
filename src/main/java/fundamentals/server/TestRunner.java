package fundamentals.server;
import org.json.*;

import java.io.*;

/**
 * Takes the POST:ed payload string from a GitHub push webhook and clones the repository specified in the payload.
 * Then, "mvn test" is run inside the repository, and if any test fails this will be noted on standard output.
 * If all tests are successful, this is printed on standard output.
 */
public class TestRunner {
    private String url;
    private String branch;
    private String repository;
    private File localRepo;
    private static final String ACCESS_TOKEN = "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";
    private static final File WORK_DIR = new File("H:/tmp/");

    /**
     * Takes the payload provided by a GitHub push webhook and runs mvn test in the repository.
     * @param payload the payload POST:ed by a GitHub webhook
     * @throws JSONException if the payload is invalid. We assume that is follows this formatting:
     *              https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#push
     */
    public TestRunner(String payload) throws JSONException {
        JSONObject obj = new JSONObject(payload);
        String strippedUrl = obj.getJSONObject("repository")
                .getString("clone_url")
                .substring("https://".length());
        url = "https://" + ACCESS_TOKEN + "@" + strippedUrl;
        branch = obj.getString("ref").substring("refs/heads/".length());
        repository = obj.getJSONObject("repository").getString("name");

        runTests();
        checkTestResults();
        cleanUp(localRepo);
    }

    private void runTests() {
        String[] cloneCmd = {"git", "clone", url};
        Runtime runtime = Runtime.getRuntime();
        System.out.println("cloning");
        try {
            runtime.exec(cloneCmd, null, WORK_DIR).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for clone!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Error running git clone in " + WORK_DIR.getAbsolutePath());
            ioException.printStackTrace();
        }

        localRepo = new File(WORK_DIR, repository);
        String[] checkoutCmd = {"git", "checkout", branch};
        System.out.println("checking out");
        try {
            runtime.exec(checkoutCmd, null, localRepo).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for checkout!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Error running git checkout in " + WORK_DIR.getAbsolutePath() + "/" + repository);
            ioException.printStackTrace();
        }

        String[] mavenCmd = {"mvn.cmd", "test"};
        System.out.println("running maven");
        try {
            runtime.exec(mavenCmd, null, localRepo).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for mvn test!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Could not run maven test in " + WORK_DIR.getAbsolutePath());
            ioException.printStackTrace();
        }

        System.out.println("Done with testing :^)!");
    }

    private void checkTestResults() {
        File testReportDirectory = new File(localRepo, "target/surefire-reports");
        var files = testReportDirectory.listFiles((File f) -> f.getName().matches(".+\\.txt"));
        for (var file : files) {
            if (!testsInReportWereSuccessful(file)) {
                System.out.println("Tests failed!");
                return;
            }
        }
        System.out.println("Tests were successful!");
    }

    private boolean testsInReportWereSuccessful(File report) {
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

    private void cleanUp(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                cleanUp(f);
            }
        }
        file.delete();
    }
}
