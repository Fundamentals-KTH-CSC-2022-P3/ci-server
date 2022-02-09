package fundamentals.server;

import java.io.IOException;

public class Compiler {
    private final RepoManager repoManager;
    private final String[] compileCmd = new String[]{"mvn", "compile"};

    public Compiler(RepoManager repoManager) {
        this.repoManager = repoManager;
    }

    /**
     * Compiles the repository using maven, and returns true if the exit code from "mvn compile" is 0.
     * @return true if the exit code from maven is 0
     * @throws IOException if the specified working directory is unreachable
     * @throws InterruptedException if the compilation process could not complete its execution.
     */
    public void compile() throws IOException, InterruptedException {
        Process compileProcess = Runtime.getRuntime().exec(compileCmd, null, repoManager.repoDir);
        compileProcess.waitFor();
        if (compileProcess.exitValue() == 0) {
            System.out.println("Compilation successful.");
        } else {
            System.out.println("Compilation failed.");
        }
    }
}
