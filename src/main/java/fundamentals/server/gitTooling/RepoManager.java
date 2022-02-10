package fundamentals.server.gitTooling;

import fundamentals.server.Environment;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * This class deals with creating, managing, and deleting the local copies of the tested repositories.
 */
public class RepoManager {
    final File workDir = new File("localFiles/");
    final File parentDir;
    final File repoDir;

    private final String accessToken;

    private final Runtime runtime = Runtime.getRuntime();

    private final String branchName;
    private final String repoUrl;

    /**
     * Create a RepoManager that manages the specified repository.
     * @param payload The payload provided by the GitHub webhook
     */
    public RepoManager(String payload, Environment environment) throws IOException {
        JSONObject obj = new JSONObject(payload);
        String strippedUrl = obj.getJSONObject("repository")
                .getString("clone_url")
                .substring("https://".length());
        accessToken = environment.getValue("ACCESS_TOKEN");
        repoUrl = "https://" + accessToken + "@" + strippedUrl;
        branchName = obj.getString("ref").substring("refs/heads/".length());
        String repositoryName = obj.getJSONObject("repository").getString("name");


        parentDir = new File(workDir, Long.toString(System.nanoTime()));
        if (!parentDir.mkdirs()) {
            throw new IOException("Could not create parent directory");
        }

        repoDir = new File(parentDir, repositoryName);
    }

    /**
     * Constructor for creating an empty git repo to be used for testing ONLY!
     */
    RepoManager() throws IOException, InterruptedException {
        parentDir = new File(workDir, Long.toString(System.nanoTime()));
        repoDir = new File(parentDir, "testRepo");
        branchName = "test-branch";
        repoUrl = "not-applicable";
        accessToken = "not-applicable";

        if (!repoDir.mkdirs()) {
            throw new IOException("Could not create repo dir in test constructor");
        }

        String[] gitInitCmd = new String[]{"git", "init"};
        runtime.exec(gitInitCmd, null, repoDir).waitFor();

        // We want to check for the branch names, but git does not output them when the repo is completely empty,
        // so we need to add a dummy file first
        String dummyFileName = "dummy";
        File dummyFile = new File(repoDir, dummyFileName);
        if (!dummyFile.createNewFile()) {
            throw new IOException();
        }
        String[] gitAddCmd = new String[]{"git", "add", dummyFileName};
        runtime.exec(gitAddCmd, null, repoDir).waitFor();
        String[] gitCommitCmd = new String[]{"git", "commit", "-m", "\"dummy\""};
        runtime.exec(gitCommitCmd, null, repoDir).waitFor();
    }

    /**
     * Clones the repository specified by the repoUrl and checks out the correct branch.
     */
    public void cloneRepo() {
        String[] cloneCmd = {"git", "clone", repoUrl};
        System.out.println("cloning");
        try {
            runtime.exec(cloneCmd, null, parentDir).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for clone!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Error running git clone in " + parentDir.getAbsolutePath());
            ioException.printStackTrace();
        }

        checkoutBranch();
    }

    /**
     * Checks out to the branch specified in the payload given in the constructor.
     */
    public void checkoutBranch() {
        String[] checkoutCmd = {"git", "checkout", branchName};
        System.out.println("checking out");
        try {
            runtime.exec(checkoutCmd, null, repoDir).waitFor();
        } catch (InterruptedException interruptedException) {
            System.err.println("Could not wait for checkout!");
            interruptedException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("Error running git checkout in " + repoDir.getAbsolutePath());
            ioException.printStackTrace();
        }
    }

    /**
     * Deletes the parent directory and all contents of the parent directory.
     */
    public void cleanUp() {
        deleteDirectory(parentDir);
    }

    /**
     * Delete a file, or recursively delete a directory
     * @param file the file or directory to be deleted
     */
    static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }

    public File getWorkDir() {
        return workDir;
    }

    public File getParentDir() {
        return parentDir;
    }

    public File getRepoDir() {
        return repoDir;
    }
}
