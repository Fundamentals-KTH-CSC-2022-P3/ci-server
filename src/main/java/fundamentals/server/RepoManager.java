package fundamentals.server;

import java.io.File;
import java.io.IOException;

public class RepoManager {
    private final File parentDir;
    private final File repoDir;

    private final Runtime runtime = Runtime.getRuntime();

    private final String branchName;
    private final String repoUrl;

    public RepoManager(String repoUrl, String repoName, String branchName, File workDir, String accessToken) throws IOException {
        this.branchName = branchName;
        this.repoUrl = "https://" + accessToken + "@" + repoUrl.substring("https://".length());

        parentDir = new File(workDir, Long.toString(System.nanoTime()));

        if (!parentDir.mkdirs()) {
            throw new IOException("Could not create parent directory");
        }

        repoDir = new File(parentDir, repoName);
    }

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
    void checkoutBranch() {
        String[] checkoutCmd = {"git", "checkout", branchName};
        System.out.println("checking out: " + branchName);
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

    public File getRepoDir() {
        return repoDir;
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
}
