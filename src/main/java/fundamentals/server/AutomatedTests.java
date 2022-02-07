package fundamentals.server;
import org.json.*;

import java.io.IOException;

public class AutomatedTests {
    String url;
    String branch;
    String username = "dd2480-ci-user";
    String password = "ktdNjqzbtQqiYHJEaDamsyVykZjPTitdTmmCaJqK";

    public AutomatedTests(String payload) throws JSONException, IOException {
        JSONObject obj = new JSONObject(payload);
        url = obj.getJSONObject("repository").getString("clone_url");
        String tmp = url.substring("https://".length());
        url = "https://" + username + ":" + password + tmp;
        branch = obj.getJSONObject("check_suite").getString("head_branch");

        Runtime.getRuntime().exec("git clone " + url);
        Runtime.getRuntime().exec("git checkout " + branch);
        Runtime.getRuntime().exec("mvn test");
    }
}
