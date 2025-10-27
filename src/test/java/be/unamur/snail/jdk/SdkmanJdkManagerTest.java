package be.unamur.snail.jdk;

import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SdkmanJdkManagerTest {
    private CommandRunner mockRunner;
    private SdkmanJdkManager manager;
    private final String sdkInitPath = "/home/test/.sdkman/bin/sdkman-init.sh";

    @BeforeEach
    void setUp() {
        mockRunner = mock(CommandRunner.class);
        manager = new SdkmanJdkManager(mockRunner, sdkInitPath);
    }

    @Test
    void isInstalledReturnsTrueWhenVersionFoundTest() throws IOException, InterruptedException {
        String version = "17.0.17-tem";
        Utils.CompletedProcess process = new Utils.CompletedProcess("cmd", 0, "some text "+ version + " some more text", "");
        when(mockRunner.run(anyString())).thenReturn(process);

        boolean result = manager.isInstalled(version);
        assertTrue(result);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRunner).run(captor.capture());
        assertTrue(captor.getValue().contains("sdk list java"));
        assertTrue(captor.getValue().contains("source " + sdkInitPath));
    }

    @Test
    void isInstalledReturnsFalseWhenVersionNotFoundTest() throws IOException, InterruptedException {
        String version = "17.0.17-tem";
        Utils.CompletedProcess process = new Utils.CompletedProcess("cmd", 0, "some text without version", "");
        when(mockRunner.run(anyString())).thenReturn(process);
        boolean result = manager.isInstalled(version);
        assertFalse(result);
    }

    @Test
    void isInstalledReturnsFalseWhenRunnerReturnsNullTest() throws IOException, InterruptedException {
        String version = "17.0.17-tem";
        when(mockRunner.run(anyString())).thenReturn(null);
        boolean result = manager.isInstalled(version);
        assertFalse(result);
    }

    @Test
    void installSuccessTest() throws IOException, InterruptedException {
        Utils.CompletedProcess success = new Utils.CompletedProcess("cmd", 0, "installation success", "");
        when(mockRunner.run(anyString())).thenReturn(success);

        manager.install("17.0.17-tem");

        verify(mockRunner).run(contains("sdk install java 17.0.17-tem"));
    }

    @Test
    void installFailureThrowsIOExceptionTest() throws IOException, InterruptedException {
        Utils.CompletedProcess failure = new Utils.CompletedProcess("cmd", 1, "", "installation failed");
        when(mockRunner.run(anyString())).thenReturn(failure);

        IOException exception = assertThrows(IOException.class, () -> {
            manager.install("17.0.17-tem");
        });

        assertTrue(exception.getMessage().contains("Failed to install JDK version 17.0.17-tem via SDKMAN!"));
    }

    @Test
    void useSuccessTest() throws IOException, InterruptedException {
        Utils.CompletedProcess success = new Utils.CompletedProcess("cmd", 0, "use success", "");
        when(mockRunner.run(anyString())).thenReturn(success);
        manager.use("17.0.17-tem");
        verify(mockRunner).run(contains("sdk use java 17.0.17-tem"));
    }

    @Test
    void useFailureThrowsIOExceptionTest() throws IOException, InterruptedException {
        Utils.CompletedProcess failure = new Utils.CompletedProcess("cmd", 1, "", "use failed");
        when(mockRunner.run(anyString())).thenReturn(failure);

        IOException exception = assertThrows(IOException.class, () -> {
            manager.use("17.0.17-tem");
        });

        assertTrue(exception.getMessage().contains("Failed to switch to JDK version 17.0.17-tem via SDK"));
    }

    @Test
    void getJavaHomeReturnsPathTest() throws IOException, InterruptedException {
        String expectedPath = "/home/test/.sdkman/candidates/java/17.0.17-tem";
        Utils.CompletedProcess process = new Utils.CompletedProcess("cmd", 0, expectedPath + "\n", "");
        when(mockRunner.run(anyString())).thenReturn(process);

        String javaHome = manager.getJavaHome("17.0.17-tem");
        assertEquals(expectedPath, javaHome);
        verify(mockRunner).run(contains("sdk home java 17.0.17-tem"));
    }

    @Test
    void getJavaHomeReturnsFallbackWhenOutputInvalidTest() throws IOException, InterruptedException {
        Utils.CompletedProcess process = new Utils.CompletedProcess("cmd", 0, "invalid output", "");
        when(mockRunner.run(anyString())).thenReturn(process);

        String javaHome = manager.getJavaHome("17.0.17-tem");
        String expectedFallback = System.getProperty("user.home") + "/.sdkman/candidates/java/17.0.17-tem";
        assertEquals(expectedFallback, javaHome);
    }
}