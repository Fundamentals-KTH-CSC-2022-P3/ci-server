package fundamentals.server;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@code BuildStorage} class.
 */
public class BuildStorageTest {

    private static final String BUILDS_TEST_FILE = "src/test/res/builds.test.json";

    /**
     * Ensure that it is possible to retrieve existing builds from the builds file on disk.
     */
    @Test
    @DisplayName("Retrieve existing builds test")
    void retrieveBuildsTest() {
        BuildStorage storage = BuildStorage.loadBuildStorageFile(BUILDS_TEST_FILE);
        assertNotNull(storage.getBuild("9ff81d98-ee33-444c-991c-8005fd6f7b62"));
        assertNotNull(storage.getBuild("3663086f-4bea-439f-a8d4-c6721b61a28e"));
        assertNotNull(storage.getBuild("c7c4fec1-a849-4a48-89fd-4d3f343f7e11"));
    }

    /**
     * Ensure that it is not possible to retrieve builds that do not exist on disk.
     */
    @Test
    @DisplayName("Cannot retrieve non-existing builds test")
    void cannotRetrieveNonExistingBuildsTest() {
        BuildStorage storage = BuildStorage.loadBuildStorageFile(BUILDS_TEST_FILE);
        assertNull(storage.getBuild("fff81d98-ee33-444c-991c-8005fd6f7b62"));
        assertNull(storage.getBuild("3663086f-424a-439f-a8d4-c6721b61a28e"));
        assertNull(storage.getBuild("c7c4fec1-a849-4a48-89fd-4d3f343f7e91"));
    }

    /**
     * Ensure that the correct amount of builds is stored in main-memory.
     */
    @Test
    @DisplayName("The correct number of builds are stored test")
    void correctNumberOfBuildsTest() {
        BuildStorage storage = BuildStorage.loadBuildStorageFile(BUILDS_TEST_FILE);
        assertEquals(3, storage.size());
    }

    /**
     * Ensure that we can retrieve the correct build information given a valid build ID.
     */
    @Test
    @DisplayName("Can retrieve the correct build information test")
    void retrieveCorrectBuildInformationTest() {
        BuildStorage storage = BuildStorage.loadBuildStorageFile(BUILDS_TEST_FILE);

        JSONObject build1 = storage.getBuild("9ff81d98-ee33-444c-991c-8005fd6f7b62");
        assertEquals("example-owner", build1.getString("owner"));
        assertEquals("example-repo", build1.getString("repository"));
        assertEquals("example-hash", build1.getString("commit"));

        JSONObject build2 = storage.getBuild("3663086f-4bea-439f-a8d4-c6721b61a28e");
        assertEquals("example-owner-2", build2.getString("owner"));
        assertEquals("example-repo-2", build2.getString("repository"));
        assertEquals("example-hash-2", build2.getString("commit"));

        JSONObject build3 = storage.getBuild("c7c4fec1-a849-4a48-89fd-4d3f343f7e11");
        assertEquals("example-owner-3", build3.getString("owner"));
        assertEquals("example-repo-3", build3.getString("repository"));
        assertEquals("example-hash-3", build3.getString("commit"));
    }

    /**
     * Ensure that we can create a new build.
     */
    @Test
    @DisplayName("Create a new build test")
    void createNewBuildTest() {
        BuildStorage storage = BuildStorage.loadBuildStorageFile(BUILDS_TEST_FILE);
        JSONObject newBuild = storage.addNewBuild("new-owner", "new-repo", "new-commit");

        assertEquals("new-owner", newBuild.getString("owner"));
        assertEquals("new-repo", newBuild.getString("repository"));
        assertEquals("new-commit", newBuild.getString("commit"));

        // Ensure that it is possible to retrieve the new build from the array.
        String buildID = newBuild.getString("build_id");
        assertEquals(newBuild, storage.getBuild(buildID));
    }
}
