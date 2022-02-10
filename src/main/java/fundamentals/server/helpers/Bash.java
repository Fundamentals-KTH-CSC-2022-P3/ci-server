package fundamentals.server.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Bash {
    private final Runtime runtime = Runtime.getRuntime();
    private List<String> stdout;

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
