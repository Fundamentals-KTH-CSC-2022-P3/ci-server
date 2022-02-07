package fundamentals.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Will handle reading key-value pairs from an environment file named ".env" that should be located at the top-level folder.
 * As for now, the ".env" file can only contain one key-value pair per line and the key should be separated from the value with a '='.
 * The ".env" file should never be added to a repository and can thereby safely contain values that must stay hidden such as Personal access tokens.
 * <p>
 * The {@code Environment} class is implemented using the Singleton pattern which means that the ".env" file gets parsed after
 * the method {@code getInstance} has been called for the first time.
 */
public class Environment {

    public static final String ENVIRONMENT_FILE_NAME = ".env";

    private static Environment instance = null;

    private HashMap<String, String> keyValuePairs = new HashMap<>();

    /**
     * Returns an instance of this class. Only one instance of this class will ever be created during program execution.
     * @return an instance of type {@code Environment}
     */
    public static Environment getInstance() {
        if (instance == null) {
            parseEnvironmentFile();
        }

        return instance;
    }

    /**
     * Parse the ".env" file.
     */
    private static void parseEnvironmentFile() {
        instance = new Environment();

        try (BufferedReader reader = new BufferedReader(new FileReader(ENVIRONMENT_FILE_NAME))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Remove whitespaces from the line.
                line = line.strip();
                // If the line starts with a '#' then we will interpret it as a comment.
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                // We only allow one key-value pair per line and the key should be separated from the value with a '='.
                String[] tokens = line.split("=");
                if (tokens.length != 2) {
                    System.err.println("Error: Only one key-value pair with the format <key>=<value> per line");
                    continue;
                }

                String key = tokens[0].strip();
                String value = tokens[1].strip();

                instance.keyValuePairs.put(key, value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will return the associated value for a given key.
     * @param key the key.
     * @return the associated value.
     */
    public String getValue(String key) {
        return keyValuePairs.get(key);
    }

    /**
     * Will return true if the given key is defined in the environment.
     * @param key the key.
     * @return true if the key is defined, otherwise false.
     */
    public boolean containsKey(String key) {
        return keyValuePairs.containsKey(key);
    }
}
