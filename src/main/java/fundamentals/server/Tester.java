package fundamentals.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Tester {
    private final File repoDir;
    private final String[] testCmd = new String[]{"mvn.cmd", "test"};
    private List<String> testOutput;

    public Tester(File repoDir) {
        this.repoDir = repoDir;
    }

    public boolean test() throws IOException, InterruptedException {
        Process testProcess = Runtime.getRuntime().exec(testCmd, null, repoDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(testProcess.getInputStream()));
        testProcess.waitFor();
        testOutput = reader.lines().toList();

        int exitValue = testProcess.exitValue();
        if (exitValue == 0) {
            System.out.println("Test successful.");
        } else {
            System.out.println("Test failed.");
        }
        return exitValue == 0;
    }

    public List<String> getTestOutput() {
        return testOutput;
    }

}
