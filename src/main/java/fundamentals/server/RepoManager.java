package fundamentals.server;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * This class deals with creating, managing, and deleting the local copies of the tested repositories.
 */
public class RepoManager {
    final File parentDir;
    final File repoDir;

    private static final String ACCESS_TOKEN = "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";

    private final Runtime runtime = Runtime.getRuntime();

    private final String branchName;
    private final String repoUrl;

    /**
     * Create a RepoManager that manages the specified repository.
     * @param payload The payload provided by the GitHub webhook
     * @param workDir The working directory, to where the repos are cloned
     */
    public RepoManager(String payload, File workDir) throws IOException {
        JSONObject obj = new JSONObject(payload);
        String strippedUrl = obj.getJSONObject("repository")
                .getString("clone_url")
                .substring("https://".length());
        repoUrl = "https://" + ACCESS_TOKEN + "@" + strippedUrl;
        branchName = obj.getString("ref").substring("refs/heads/".length());
        String repositoryName = obj.getJSONObject("repository").getString("name");


        parentDir = new File(workDir, Long.toString(System.nanoTime()));
        if (!parentDir.mkdirs()) {
            throw new IOException("Could not create parent directory");
        }

        repoDir = new File(parentDir, repositoryName);
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

    void checkoutBranch() {
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

    static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }
}