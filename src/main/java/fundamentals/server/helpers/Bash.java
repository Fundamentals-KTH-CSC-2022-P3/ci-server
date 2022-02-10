package fundamentals.server.helpers;

import java.io.File;
import java.io.IOException;

public class Bash {
    private final Runtime runtime = Runtime.getRuntime();

    public boolean execute(String[] cmdArray, String[] envArr, File dir) {
        Process process = null;
        try {
            process = runtime.exec(cmdArray, envArr, dir);
            process.waitFor();
        } catch (InterruptedException interruptedException) {
            return false;
        } catch (IOException ioException) {
            return false;
        }
        return process.exitValue() == 0;
    }
}
