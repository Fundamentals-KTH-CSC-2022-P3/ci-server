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
 */
public class Environment {

    public static final String DEFAULT_ENVIRONMENT_FILE = ".env";

    private final HashMap<String, String> keyValuePairs = new HashMap<>();

    /**
     * Creates an instance of the {@code Environment} class and loads the environment file from disk into main-memory.
     * The instance will store all the key-value pairs from the environment file.
     *
     * @param filePath the path to the environment file.
     */
    public static Environment loadEnvironmentFile(String filePath) {
        Environment environment = new Environment();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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

                environment.keyValuePairs.put(key, value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return environment;
    }

    /**
     * Creates an instance of the {@code Environment} class and loads the environment file ".env" from disk into main-memory.
     * The instance will store all the key-value pairs from the environment file.
     */
    public static Environment loadEnvironmentFile() {
        return loadEnvironmentFile(DEFAULT_ENVIRONMENT_FILE);
    }

    public static String loadEnvironmentVariableOrElse(String environmentVariableName, String orElse) {
        var sysVariable = System.getenv(environmentVariableName);
        if (sysVariable == null) {
            return orElse;
        }
        return sysVariable;
    }

    // We want the programmer to use loadEnvironmentFile() to create an instance of this class and never the constructor.
    private Environment() {
    }

    /**
     * Will return the associated value for a given key.
     *
     * @param key the key.
     * @return the associated value.
     */
    public String getValue(String key) {
        return keyValuePairs.get(key);
    }

    /**
     * Will return true if the given key is defined in the environment.
     *
     * @param key the key.
     * @return true if the key is defined, otherwise false.
     */
    public boolean containsKey(String key) {
        return keyValuePairs.containsKey(key);
    }

    /**
     * Will return the number of key-value pairs defined in the environment.
     *
     * @return the number of defined key-value pairs.
     */
    public int size() {
        return keyValuePairs.size();
    }
}
