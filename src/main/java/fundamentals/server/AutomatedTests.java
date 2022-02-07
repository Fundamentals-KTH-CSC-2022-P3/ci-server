package fundamentals.server;
import org.json.*;

import java.io.*;
import java.nio.file.StandardCopyOption;

public class AutomatedTests {
    private String url;
    private String branch;
    private String repository;
    private File localRepo;
    private static final String ACCESS_TOKEN = "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";
    private static final File WORK_DIR = new File("H:/tmp/");

    public AutomatedTests(String payload) throws JSONException, IOException {
        JSONObject obj = new JSONObject(payload);
        url = obj.getJSONObject("repository").getString("clone_url");
        url = url.substring("https://".length());
        url = "https://" + ACCESS_TOKEN + "@" + url;
        branch = obj.getString("ref").substring("refs/heads/".length());
        repository = obj.getJSONObject("repository").getString("name");

        runTests();
        checkTestResults();
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
        File testOutputFile = new File(WORK_DIR, "maven-test-out.txt");
        try {
            Process mavenTestProcess = runtime.exec(mavenCmd, null, localRepo);
            mavenTestProcess.waitFor();
            java.nio.file.Files.copy(
                mavenTestProcess.getInputStream(),
                testOutputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for mvn test!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Could not create maven test log file in " + WORK_DIR.getAbsolutePath());
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
        return reader.lines().anyMatch(s -> s.matches("Tests run: \\d+, Failures: 0,.+"));
    }
}
