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
 * Everytime the server restarts the builds.json file will be loaded into main-memory. Use the {@code saveToDisk}
 * method when a new build is added to ensure that we keep an updated version of the builds on disk.
 */
public class BuildStorage {

    public static final String DEFAULT_BUILD_STORAGE_FILE = "builds.json";

    // The build ID is a universally unique identifier (UUID).
    // If we don't want to do a linear search when the user asks for information about a build with a certain build ID,
    // then we should keep a lookup table that maps each build ID to an array index.
    private HashMap<String, Integer> buildIDToArrayIndex = new HashMap<>();

    // All the builds are stored here.
    private JSONArray builds;

    // The path to the file where we should save all our builds.
    private String filePath;

    /**
     * Creates an instance of the {@code BuildStorage} class and loads the builds file from disk into main-memory.
     * This method should only be called from unit-tests. If you are not working on unit tests then call the {@code loadBuildStorageFile()}
     * method instead that will use the default "builds.json" file.
     *
     * @param filePath the path to the builds file.
     */
    public static BuildStorage loadBuildStorageFile(String filePath) {
        BuildStorage storage = new BuildStorage();
        storage.filePath = filePath;

        // Read the builds from disk into main-memory.
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }

            // Parse the JSON text to an actual JSONArray object.
            storage.builds = new JSONArray(jsonText.toString());

            // Create mappings from build ID to array index (this is only done for performance).
            for (int i = 0; i < storage.builds.length(); i++) {
                JSONObject build = storage.builds.getJSONObject(i);
                String buildID = build.getString("build_id");
                storage.buildIDToArrayIndex.put(buildID, i);
            }
        } catch (FileNotFoundException e) {
            // Builds file does not exist. Create a builds file with an empty array inside.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
                writer.write("[]");
                storage.builds = new JSONArray();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return storage;
    }

    /**
     * Creates an instance of the {@code BuildStorage} class and loads the builds file from disk into main-memory.
     */
    public static BuildStorage loadBuildStorageFile() {
        return loadBuildStorageFile(DEFAULT_BUILD_STORAGE_FILE);
    }

    // We want the programmer to use loadBuildStorageFile() to create an instance of this class and never the constructor.
    private BuildStorage() {
    }

    /**
     * Will add a new build to the array stored in main-memory. However, this call will not update the file on disk.
     * If you want to update the file on disk then you have to call {@code saveToDisk()} after you have
     * added your build.
     *
     * @param commitHash the commit hash.
     * @param repository the repository.
     * @param owner      the owner of the repository.
     * @return an object of type {@code JSONObject} that contains information about the current build.
     */
    public JSONObject addNewBuild(String commitHash, String repository, String owner) {
        JSONObject build = new JSONObject();

        // Generate a unique identifier for this build.
        String buildID = UUID.randomUUID().toString();

        // The information we will store about each build.
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

        // Add the build to the array and also store the mapping from build ID -> array index.
        builds.put(build);
        buildIDToArrayIndex.put(buildID, buildIDToArrayIndex.size());

        return build;
    }

    /**
     * Returns the number of builds stored in main-memory.
     *
     * @return number of builds.
     */
    public int size() {
        return builds.length();
    }

    /**
     * Returns a JSON object that contains information about a build given the build ID.
     *
     * @param buildID specify which build you want information about using the build ID.
     * @return an object of type {@code JSONObject} that contains relevant information about the build.
     */
    public JSONObject getBuild(String buildID) {
        // If we can't find a build with that ID then we will return null.
        if (!buildIDToArrayIndex.containsKey(buildID))
            return null;

        int index = buildIDToArrayIndex.get(buildID);

        return builds.getJSONObject(index);
    }

    /**
     * Returns the whole JSON array that contains information about all builds.
     *
     * @return an object of type {@code JSONArray} that contains information about all builds.
     */
    public JSONArray getAllBuilds() {
        return builds;
    }

    /**
     * Will write all builds that are stored in main-memory to a file on disk.
     */
    public synchronized void saveToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(builds.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
