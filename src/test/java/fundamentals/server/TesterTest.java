package fundamentals.server;

import fundamentals.server.helpers.Bash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@code Tester} class.
 */
public class TesterTest {

    private static String PATH_TO_WORKING_DIRECTORY = "/path/to/some/dir";

    private Tester tester;

    @BeforeEach
    private void setUp() throws Exception {
    }

    @AfterEach
    private void tearDown() throws Exception {
    }

    @Test
    @DisplayName("Test that maven test is run by tester")
    void testThatTesterWillExecuteMavenTest() {
        String[] cmd = {"mvn", "test"};
        File dir = new File(PATH_TO_WORKING_DIRECTORY);
        Bash shell = mock(Bash.class);

        when(shell.execute(cmd, null, dir)).thenReturn(true);

        tester = new Tester(dir, shell);

        assertTrue(tester.run());
        verify(shell, times(1)).execute(cmd, null, dir);
    }

    @Test
    @DisplayName("Test that a non-zero exit code produces a failing test")
    void testThatANonZeroExitCodeProducesAFailingTest() {
        String[] cmd = {"mvn", "test"};
        File dir = new File(PATH_TO_WORKING_DIRECTORY);
        Bash shell = mock(Bash.class);

        when(shell.execute(cmd, null, dir)).thenReturn(false);

        tester = new Tester(dir, shell);

        assertFalse(tester.run());
        verify(shell, times(1)).execute(cmd, null, dir);
    }
}
