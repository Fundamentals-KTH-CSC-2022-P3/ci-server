package fundamentals.server;

import org.json.JSONArray;
import java.io.*;
import java.nio.charset.StandardCharsets;

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
}
