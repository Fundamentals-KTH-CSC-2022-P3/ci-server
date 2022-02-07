package fundamentals.server;
import org.json.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class AutomatedTests {
    String url;
    String branch;
    String repository;
    private static final String ACCESS_TOKEN = "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";
    private static final File WORK_DIR = new File("H:/tmp/");

    public AutomatedTests(String payload) throws JSONException, IOException {
        JSONObject obj = new JSONObject(payload);
        url = obj.getJSONObject("repository").getString("clone_url");
        url = url.substring("https://".length());
        url = "https://" + ACCESS_TOKEN + "@" + url;
        branch = obj.getString("ref").substring("refs/heads/".length());
        repository = obj.getJSONObject("repository").getString("name");

        String[] cloneCmd = {"git", "clone", url};
        Runtime runtime = Runtime.getRuntime();
        System.out.println("cloning");
        try {
            runtime.exec(cloneCmd, null, WORK_DIR).waitFor();
        } catch (InterruptedException e) {
            System.err.println("Could not wait for clone!");
            e.printStackTrace();
        }

        File repoDirectory = new File(WORK_DIR, repository);
        String[] checkoutCmd = {"git", "checkout", branch};
        System.out.println("checking out");
        try {
            runtime.exec(checkoutCmd, null, repoDirectory).waitFor();
        } catch (InterruptedException e) {
            System.err.println("Could not wait for checkout!");
            e.printStackTrace();
        }

        String[] mavenCmd = {"mvn.cmd", "test"};
        System.out.println("running maven");
        File testOutputFile = new File(WORK_DIR, "maven-test-out.txt");
        Process mavenTestProcess = runtime.exec(mavenCmd, null, repoDirectory);
        try {
            mavenTestProcess.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Could not wait for mvn test!");
            e.printStackTrace();
        }
        java.nio.file.Files.copy(
                mavenTestProcess.getInputStream(),
                testOutputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Done with testing :^)!");
    }

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("mvn.cmd");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
