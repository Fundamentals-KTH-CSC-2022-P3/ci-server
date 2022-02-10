package fundamentals.server;

import fundamentals.server.helpers.Bash;

import java.io.*;

/**
 * Takes the POST:ed payload string from a GitHub push webhook and clones the repository specified in the payload.
 * Then, "mvn test" is run inside the repository, and if any test fails this will be noted on standard output.
 * If all tests are successful, this is printed on standard output.
 */
public class Tester {

    private final File repoDir;
    private Bash shell;

    /**
     * Takes a RepoManager that is connected to the repo to test, and tests the repo, reporting any
     * failures.
     * @param repoDir the root of the local copy of the repository to test
     */
    public Tester(File repoDir, Bash shell) {
        this.repoDir = repoDir;
        this.shell = shell;
    }

    public boolean run() {
        String[] mavenCmd = {"mvn", "test"};
        System.out.println("running maven");
        return this.shell.execute(mavenCmd, null, repoDir);
    }
}
