package fundamentals.server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a persistent storage that will store all builds both in main-memory but also on disk in JSON format.
 * Everytime the server restarts the builds.json file will be loaded into main-memory.
 * <p>
 * The {@code BuildStorage} class is implemented using the Singleton pattern which means that the "builds.json" file gets loaded after
 * the method {@code getInstance} has been called for the first time.
 */
public class BuildStorage {

    public static final String DEFAULT_BUILD_STORAGE_FILE = "builds.json";

    private static BuildStorage instance;

    private HashMap<String, Integer> buildIDToArrayIndex = new HashMap<>();
    private JSONArray builds;
    private String filePath;

    public static BuildStorage getInstance(String filePath) {
        if (instance == null) {
            loadBuildStorageFile(filePath);
        }

        return instance;
    }

    public static BuildStorage getInstance() {
        return getInstance(DEFAULT_BUILD_STORAGE_FILE);
    }

    private static void loadBuildStorageFile(String filePath) {
        instance = new BuildStorage();
        instance.filePath = filePath;

        // Read the builds from disk into main-memory.
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            instance.builds = new JSONArray(jsonText.toString());

            for (int i = 0; i < instance.builds.length(); i++) {
                JSONObject build = instance.builds.getJSONObject(i);
                String buildID = build.getString("build_id");
                instance.buildIDToArrayIndex.put(buildID, i);
            }
        } catch (FileNotFoundException e) {
            // Builds file does not exist. Create a builds.json file with an empty array inside.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
                writer.write("[]");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject addNewBuild(String commitHash, String repository, String owner) {
        JSONObject build = new JSONObject();

        // Generate a unique identifier for this build.
        String buildID = UUID.randomUUID().toString();

        build.put("build_id", buildID);
        build.put("commit", commitHash);
        build.put("repository", repository);
        build.put("owner", owner);
        build.put("build_started", Instant.now().toString());
        build.put("build_ended", "Still running");
        build.put("compile_status", "pending");
        build.put("test_status", "pending");
        build.put("compile_logs", new JSONArray());
        build.put("test_logs", new JSONArray());

        builds.put(build);
        buildIDToArrayIndex.put(buildID, buildIDToArrayIndex.size() - 1);

        return build;
    }

    public JSONObject getBuild(String buildID) {
        // If we can't find a build with that ID then we will return null.
        if (!buildIDToArrayIndex.containsKey(buildID))
            return null;

        int index = buildIDToArrayIndex.get(buildID);

        return builds.getJSONObject(index);
    }

    public void saveToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(builds.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
