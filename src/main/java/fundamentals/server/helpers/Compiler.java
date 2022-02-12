package fundamentals.server.helpers;

import java.io.*;
import java.util.List;

/**
 * A class for managing compilation of the tested repository. This is done via Maven.
 */
public class Compiler {

    private final File repoDir;
    private Bash shell;

    /**
     * Create a new compiler instance
     *
     * @param repoDir the directory where source code will exist
     * @param shell a shell that will execute the compilation commands
     */
    public Compiler(File repoDir, Bash shell) {
        this.repoDir = repoDir;
        this.shell = shell;
    }

    /**
     * Compiles the repository using maven, and returns true if the exit code from "mvn compile" is 0.
     * @return true if the compilation was successful, as determined by the exit code of "mvn compile"
     */
    public boolean compile() {
        String[] mavenCmd = {"mvn", "compile"};

        return shell.execute(mavenCmd, null, repoDir);
    }

    /**
     * Get the output from the compilation command
     * @return a list of the lines of the compilation output
     */
    public List<String> getCompileOutput() {
        return shell.getStdout();
    }
}
