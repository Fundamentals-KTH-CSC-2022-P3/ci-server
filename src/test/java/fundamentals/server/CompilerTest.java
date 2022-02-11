package fundamentals.server;

import fundamentals.server.helpers.Bash;
import fundamentals.server.helpers.Compiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

/**
 * Tests for the {@code Compiler} class.
 */
public class CompilerTest {

    private static String PATH_TO_WORKING_DIRECTORY = "/path/to/some/dir";

    private Compiler compiler;

    @BeforeEach
    private void setUp() throws Exception {
    }

    @AfterEach
    private void tearDown() throws Exception {
    }

    @Test
    @DisplayName("Test compiler runs mvn compile")
    void testThatTesterWillExecuteMavenCompile() {
        String[] cmd = {"mvn", "compile"};
        File dir = new File(PATH_TO_WORKING_DIRECTORY);
        Bash shell = mock(Bash.class);

        when(shell.execute(cmd, null, dir)).thenReturn(false);

        compiler = new Compiler(dir, shell);
        compiler.compile();

        verify(shell, times(1)).execute(cmd, null, dir);
    }

    @Test
    @DisplayName("Test compile returns true if exit code is zero")
    void testThatCompileReturnsTrueIfExitCodeFromMavenCompileIsZero() {
        String[] cmd = {"mvn", "compile"};
        File dir = new File(PATH_TO_WORKING_DIRECTORY);
        Bash shell = mock(Bash.class);

        when(shell.execute(cmd, null, dir)).thenReturn(true);

        compiler = new Compiler(dir, shell);

        assertFalse(compiler.compile());
        verify(shell, times(1)).execute(cmd, null, dir);
    }

    @Test
    @DisplayName("Test compile returns false if exit code is not zero")
    void testThatCompileReturnsTrueIfNonZeroExitCodeFromMavenCompile() {
        String[] cmd = {"mvn", "compile"};
        File dir = new File(PATH_TO_WORKING_DIRECTORY);
        Bash shell = mock(Bash.class);

        when(shell.execute(cmd, null, dir)).thenReturn(false);

        compiler = new Compiler(dir, shell);

        assertFalse(compiler.compile());
        verify(shell, times(1)).execute(cmd, null, dir);
    }
}
