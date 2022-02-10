package fundamentals.server;

import fundamentals.server.helpers.Bash;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Compiler {

    private final File repoDir;
    private Bash shell;
    private List<String> compileOutput;

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

        return this.shell.execute(mavenCmd, null, repoDir);
    }

    public List<String> getCompileOutput() {
        return this.shell.getStdout();
    }
}
