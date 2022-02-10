package fundamentals.server;

import java.io.*;
import java.util.stream.Collectors;

public class Compiler {
    private final File repoDir;
    private final String[] compileCmd = new String[]{"mvn", "compile"};
    private String compileOutput;

    public Compiler(File repoDir) {
        this.repoDir = repoDir;
    }

    /**
     * Compiles the repository using maven, and returns true if the exit code from "mvn compile" is 0.
     * @return true if the compilation was successful, as determined by the exit code of "mvn compile"
     * @throws IOException if the specified working directory is unreachable
     * @throws InterruptedException if the compilation process could not complete its execution.
     */
    public boolean compile() throws IOException, InterruptedException {
        Process compileProcess = Runtime.getRuntime().exec(compileCmd, null, repoDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
        compileProcess.waitFor();
        compileOutput = reader.lines().collect(Collectors.joining("\n"));

        int exitValue = compileProcess.exitValue();
        if (exitValue == 0) {
            System.out.println("Compilation successful.");
        } else {
            System.out.println("Compilation failed.");
        }
        return exitValue == 0;
    }

    public String getCompileOutput() {
        return compileOutput;
    }
}
