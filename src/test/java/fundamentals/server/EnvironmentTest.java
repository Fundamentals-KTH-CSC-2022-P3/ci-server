package fundamentals.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentTest {

    private static final String ENVIRONMENT_TEST_FILE = "src/test/res/.env.test";

    /**
     * Ensure that the {@code Environment} object contains all the keys defined in the file.
     */
    @Test
    @DisplayName("All keys can be found test")
    void containsAllKeysTest() {
        Environment env = Environment.getInstance(ENVIRONMENT_TEST_FILE);
        assertTrue(env.containsKey("NAME"));
        assertTrue(env.containsKey("SENTENCE"));
        assertTrue(env.containsKey("A COOL KEY"));
        assertTrue(env.containsKey("TEST"));
        assertTrue(env.containsKey("TEST2"));
    }

    /**
     * Ensure that the {@code Environment} object stores the correct amount of key-value pairs.
     */
    @Test
    @DisplayName("The correct number of key-value pairs are stored test")
    void correctNumberOfKeyValuePairsTest() {
        Environment env = Environment.getInstance(ENVIRONMENT_TEST_FILE);
        assertEquals(6, env.size());
    }

    /**
     * Ensure that the {@code Environment} object can retrieve the correct value for each key.
     */
    @Test
    @DisplayName("Can retrieve the correct value for each key test")
    void retrieveKeyValuePairsFromEnvironmentTest() {
        Environment env = Environment.getInstance(ENVIRONMENT_TEST_FILE);
        assertEquals("example-name", env.getValue("NAME"));
        assertEquals("this is a sentence", env.getValue("SENTENCE"));
        assertEquals("example-password", env.getValue("PASSWORD"));
        assertEquals("not so cool", env.getValue("A COOL KEY"));
        assertEquals("example-test",env.getValue("TEST"));
        assertEquals("example-test2",env.getValue("TEST2"));
    }
}
