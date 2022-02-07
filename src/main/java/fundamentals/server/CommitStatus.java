package fundamentals.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CommitStatus {

    public static final String USERNAME = "alevarn";
    public static final String TOKEN = "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";
    public static final String USER_TOKEN = Base64.getEncoder().encodeToString((USERNAME + ":" + TOKEN).getBytes(StandardCharsets.UTF_8));
    public static final String CONTEXT = "CI-server";

    public static final String GITHUB_API = "https://api.github.com";

    /**
     * Set the commit status of a commit.
     */
    public static void set(String owner, String repo, String sha, String state, String description, String targetUrl) {
        try {
            var url = new URL(GITHUB_API + "/repos/" + owner + "/" + repo + "/statuses/" + sha);
            var http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept", "application/vnd.github.v3+json");
            http.setRequestProperty("Authorization", "Basic " + USER_TOKEN);
            byte[] json = ("{" + "\"state\":" + "\"" + state + "\"" + "," + "\"description\":" + "\"" + description + "\"" + "," + "\"context\":" + "\"" + CONTEXT + "\"" + "," + "\"target_url\":" + "\"" + targetUrl + "\"" + "}").getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(json.length);
            http.connect();

            OutputStream out = http.getOutputStream();
            out.write(json);

            System.out.println(http.getResponseCode());

            // Check the input!
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        set("Fundamentals-KTH-CSC-2022-P3", "set-commit-status-test", "b7a0c7217c2a40676da1b666d706c09fcda8ba4c", "failure", "The CI-server found errors", "https://www.google.se");
    }

}
