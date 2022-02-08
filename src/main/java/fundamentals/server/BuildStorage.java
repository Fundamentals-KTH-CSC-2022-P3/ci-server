package fundamentals.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

public class BuildStorage {

    public static final String BUILD_STORAGE_FILE = "builds.json";

    private static BuildStorage instance;

    private JSONArray builds;

    public static BuildStorage getInstance() {
        if (instance == null) {
            loadBuildStorageFile();
        }

        return instance;
    }

    private static void loadBuildStorageFile() {
        instance = new BuildStorage();

        // Read the builds from disk into main-memory.
        try (BufferedReader reader = new BufferedReader(new FileReader(BUILD_STORAGE_FILE, StandardCharsets.UTF_8))) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            instance.builds = new JSONArray(jsonText.toString());
        } catch (FileNotFoundException e) {
            // Builds file does not exist. Create a builds.json file with an empty array inside.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(BUILD_STORAGE_FILE, StandardCharsets.UTF_8))){
                writer.write("[]");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject addNewBuild(String commitSha, String repository, String owner) {
        JSONObject build = new JSONObject();
        build.put("build_id", UUID.randomUUID());
        build.put("commit", commitSha);
        build.put("repository", repository);
        build.put("owner", owner);
        build.put("build_started", Instant.now().toString());
        build.put("compile_status", "pending");
        build.put("test_status", "pending");
        build.put("compile_logs", new JSONArray());
        build.put("test_logs", new JSONArray());
        builds.put(build);
        return build;
    }

    public JSONObject findBuild(String buildID) {
        // A good idea is probably to keep an index that maps build_id to position in array.
        // Then we don't have to do a linear search each time.
        for (int i = 0; i < builds.length(); i++) {
            JSONObject build = builds.getJSONObject(i);
            if (build.getString("build_id").equals(buildID)) {
                return build;
            }
        }
        // Could not find a build with the requested build ID.
        return null;
    }

    public boolean setBuildCompileStatus(String buildID, String status) {
        return setBuildValue(buildID, "compile_status", status);
    }

    public boolean setBuildTestStatus(String buildID, String status) {
        return setBuildValue(buildID, "test_status", status);
    }

    public boolean setBuildEndedTime(String buildID) {
        return setBuildValue(buildID, "build_ended", Instant.now().toString());
    }

    private boolean setBuildValue(String buildID, String key, String value) {
        JSONObject build = findBuild(buildID);
        if (build == null)
            return false;
        build.put(key, value);
        return true;
    }

    public boolean setBuildCompileLogs(String buildID, ArrayList<String> logs) {
        return setBuildArrayValues(buildID, "compile_logs", logs);
    }

    public boolean setBuildTestLogs(String buildID, ArrayList<String> logs) {
        return setBuildArrayValues(buildID, "test_logs", logs);
    }

    private boolean setBuildArrayValues(String buildID, String key, ArrayList<String> values) {
        JSONObject build = findBuild(buildID);
        if (build == null)
            return false;
        JSONArray array = build.getJSONArray(key);
        for (String value : values)
            array.put(value);
        return true;
    }

    public void saveToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BUILD_STORAGE_FILE, StandardCharsets.UTF_8))){
            writer.write(builds.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
