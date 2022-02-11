package fundamentals.server.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Helper class for running "exec()" commands and capturing their output in a more ergonomic way than using
 * Runtime.exec() directly.
 */
public class Bash {
    private final Runtime runtime = Runtime.getRuntime();
    private List<String> stdout;

    /**
     * Spawns a process by executing a command, and blocks while the process is running.
     *
     * @param cmdArray the command to be executed, each argument a separate element in the array
     * @param envArr   an array of environment variables for the execution
     * @param dir      the working directory to execute in
     * @return true iff the process exited normally with exit code 0
     */
    public boolean execute(String[] cmdArray, String[] envArr, File dir) {
        Process process = null;
        BufferedReader reader = null;

        try {
            process = runtime.exec(cmdArray, envArr, dir);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
        } catch (InterruptedException interruptedException) {
            return false;
        } catch (IOException ioException) {
            return false;
        } finally {
            stdout = reader.lines().toList();
        }
        return process.exitValue() == 0;
    }

    public List<String> getStdout() {
        return stdout;
    }
}
