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
 * <p>
 * The {@code BuildStorage} class is implemented using the Singleton pattern which means that the "builds.json" file gets loaded after
 * the method {@code getInstance} has been called for the first time.
 */
public class BuildStorage {

    public static final String DEFAULT_BUILD_STORAGE_FILE = "builds.json";

    private static BuildStorage instance;

    // The build ID is a universally unique identifier (UUID).
    // If we don't want to do a linear search when the user asks for information about a build with a certain build ID,
    // then we should keep a lookup table that maps each build ID to an array index.
    private HashMap<String, Integer> buildIDToArrayIndex = new HashMap<>();

    // All the builds are stored here.
    private JSONArray builds;

    // The path to the file where we should save all our builds.
    private String filePath;

    /**
     * Returns an instance of this class. Only one instance of this class will ever be created during program execution.
     * This method should only be called from unit-tests. If you are not working on unit tests then call the {@code getInstance()}
     * method instead that will use the default "builds.json" file.
     *
     * @return an instance of type {@code BuildStorage}
     */
    public static BuildStorage getInstance(String filePath) {
        if (instance == null) {
            loadBuildStorageFile(filePath);
        }

        return instance;
    }

    /**
     * Returns an instance of this class. Only one instance of this class will ever be created during program execution.
     * The instance will store all the builds from the "builds.json" file.
     *
     * @return an instance of type {@code BuildStorage}
     */
    public static BuildStorage getInstance() {
        return getInstance(DEFAULT_BUILD_STORAGE_FILE);
    }

    /**
     * Creates an instance of the {@code BuildStorage} class and loads the builds file from disk into main-memory.
     *
     * @param filePath the path to the builds file.
     */
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

            // Parse the JSON text to an actual JSONArray object.
            instance.builds = new JSONArray(jsonText.toString());

            // Create mappings from build ID to array index (this is only done for performance).
            for (int i = 0; i < instance.builds.length(); i++) {
                JSONObject build = instance.builds.getJSONObject(i);
                String buildID = build.getString("build_id");
                instance.buildIDToArrayIndex.put(buildID, i);
            }
        } catch (FileNotFoundException e) {
            // Builds file does not exist. Create a builds file with an empty array inside.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
                writer.write("[]");
                instance.builds = new JSONArray();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        buildIDToArrayIndex.put(buildID, buildIDToArrayIndex.size() - 1);

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
     * Will write all builds that are stored in main-memory to a file on disk.
     */
    public void saveToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(builds.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
