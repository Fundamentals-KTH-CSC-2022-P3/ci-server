package fundamentals.server;

import java.io.*;

/**
 * Takes the POST:ed payload string from a GitHub push webhook and clones the repository specified in the payload.
 * Then, "mvn test" is run inside the repository, and if any test fails this will be noted on standard output.
 * If all tests are successful, this is printed on standard output.
 */
public class Tester {
    private final File repoDir;
    private final Runtime runtime = Runtime.getRuntime();

    /**
     * Takes a RepoManager that is connected to the repo to test, and tests the repo, reporting any
     * failures.
     * @param repoDir the root of the local copy of the repository to test
     */
    public Tester(File repoDir) {
        this.repoDir = repoDir;
    }

    public boolean run() {
        String[] mavenCmd = {"mvn.cmd", "test"};
        System.out.println("running maven");
        Process mavenProcess = null;
        try {
            mavenProcess = runtime.exec(mavenCmd, null, repoDir);
            mavenProcess.waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for mvn test!");
            interruptedException.printStackTrace();
            return false;
        } catch (IOException ioException) {
            System.err.println("Could not run maven test in " + repoDir.getAbsolutePath());
            ioException.printStackTrace();
            return false;
        }
        // TODO: set status of remote repository
        return mavenProcess.exitValue() == 0;
    }
}
