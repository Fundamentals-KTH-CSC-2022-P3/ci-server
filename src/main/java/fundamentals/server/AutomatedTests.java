package fundamentals.server;
import org.json.*;

import java.io.File;
import java.io.IOException;
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

        String[] mavenCmd = {"mvn", "test"};
        System.out.println("running maven");
        try {
            runtime.exec(mavenCmd, null, repoDirectory).waitFor();
        } catch (InterruptedException e) {
            System.err.println("Could not wait for mvn test!");
            e.printStackTrace();
        }

        System.out.println("Done with testing :^)!");
    }
}
