package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.jdk.JdkManager;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetupJdkStageTest {

    private JdkManager manager;
    private Config config;
    private Config.RepoConfig repo;
    private Context context;
    private CommandRunner runner;
    private SetupJdkStage stage;
    private PipelineLogger log;

    @BeforeEach
    void setUp() {
        manager = mock(JdkManager.class);
        config = mock(Config.class);
        repo = mock(Config.RepoConfig.class);
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(SetupJdkStage.class));
        runner = mock(CommandRunner.class);
        log = mock(PipelineLogger.class);

        when(config.getRepo()).thenReturn(repo);
        stage = new SetupJdkStage(manager, config, runner);
    }

    @Test
    void executeWithExistingJdkTest() throws Exception {
        String jdkVersion = "17.0.17-tem";
        String javaHome = "/path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(repo.getJdk()).thenReturn(jdkVersion);
        when(manager.isInstalled(jdkVersion)).thenReturn(true);
        when(manager.getJavaHome(jdkVersion)).thenReturn("/path/to/jdk17");

        Utils.CompletedProcess ok = new Utils.CompletedProcess("", 0, "", "");
        Utils.CompletedProcess check = new Utils.CompletedProcess("", 0, javaBinary + "\n", "");

        when(runner.run("update-alternatives --query java")).thenReturn(check);
        when(runner.run("sudo update-alternatives --set java " + javaBinary)).thenReturn(ok);
        when(runner.run("export JAVA_HOME=" + javaHome)).thenReturn(ok);

        stage.execute(context);

        verify(manager, never()).install(jdkVersion);
        verify(manager).use(jdkVersion);
        verify(manager).getJavaHome(jdkVersion);

        verify(runner).run("export JAVA_HOME=/path/to/jdk17");

        assertEquals("/path/to/jdk17", context.getJavaHome());
    }

    @Test
    void executeWithUninstalledJdkTest() throws Exception {
        String jdkVersion = "17.0.17-tem";
        String javaHome = "/path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(repo.getJdk()).thenReturn(jdkVersion);
        when(manager.isInstalled(jdkVersion)).thenReturn(false);
        when(manager.getJavaHome(jdkVersion)).thenReturn("/path/to/jdk17");

        Utils.CompletedProcess check = new Utils.CompletedProcess("", 0, "", "");
        Utils.CompletedProcess ok = new Utils.CompletedProcess("", 0, "", "");
        when(runner.run("update-alternatives --query java")).thenReturn(check);
        when(runner.run("sudo update-alternatives --install /usr/bin/java java " + javaBinary + " 1")).thenReturn(ok);
        when(runner.run("sudo update-alternatives --set java " + javaBinary)).thenReturn(ok);
        when(runner.run("export JAVA_HOME=" + javaHome)).thenReturn(ok);

        stage.execute(context);

        verify(manager).install(jdkVersion);
        verify(manager).use(jdkVersion);

        verify(runner).run("export JAVA_HOME=/path/to/jdk17");
        assertEquals("/path/to/jdk17", context.getJavaHome());
    }

    @Test
    void executeShouldThrowExceptionWhenJdkVersionNullOrBlankTest() {
        when(repo.getJdk()).thenReturn(null);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));

        when(repo.getJdk()).thenReturn("");
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeShouldThrowExceptionWhenJavaHomeBlankOrNullTest() throws IOException, InterruptedException {
        String jdkVersion = "17.0.17-tem";
        when(repo.getJdk()).thenReturn(jdkVersion);
        when(manager.isInstalled(jdkVersion)).thenReturn(true);

        when(manager.getJavaHome(jdkVersion)).thenReturn(null);
        assertThrows(Exception.class, () -> stage.execute(context));

        when(manager.getJavaHome(jdkVersion)).thenReturn("");
        assertThrows(Exception.class, () -> stage.execute(context));
    }

    @Test
    void updateAlternativesWhenNotRegisteredTest() throws IOException, InterruptedException {
        String javaHome = "/path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(runner.run("update-alternatives --query java"))
                .thenReturn(new Utils.CompletedProcess("", 0, "Some other java\n", ""));
        when(runner.run("sudo update-alternatives --install /usr/bin/java java " + javaBinary + " 1"))
                .thenReturn(new Utils.CompletedProcess("", 0, "", ""));
        when(runner.run("sudo update-alternatives --set java " + javaBinary))
                .thenReturn(new Utils.CompletedProcess("", 0, "", ""));

        assertDoesNotThrow(() -> stage.updateAlternatives(log, javaHome));

        verify(log).info(contains("Registering"), anyString());
        verify(log).info(contains("Setting"), anyString());
        verify(runner).run(contains("--install"));
        verify(runner).run(contains("--set"));
    }

    @Test
    void updateAlternativesWhenAlreadyRegisteredTest() throws IOException, InterruptedException {
        String javaHome = "/path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(runner.run("update-alternatives --query java"))
                .thenReturn(new Utils.CompletedProcess("", 0, "Some line\n" + javaBinary + "\n", ""));
        when(runner.run("sudo update-alternatives --set java " + javaBinary))
                .thenReturn(new Utils.CompletedProcess("", 0, "", ""));

        assertDoesNotThrow(() -> stage.updateAlternatives(log, javaHome));

        verify(log).info(contains("already registered"), anyString());
        verify(log).info(contains("Setting"), anyString());
        verify(runner, never()).run(contains("--install"));
        verify(runner).run(contains("--set"));
    }

    @Test
    void updateAlternativesWhenInstallFailsShouldThrowExceptionTest() throws IOException, InterruptedException {
        String javaHome = "path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(runner.run("update-alternatives --query java"))
                .thenReturn(new Utils.CompletedProcess("", 0, "Some other java\n", ""));
        when(runner.run("sudo update-alternatives --install /usr/bin/java java " + javaBinary + " 1"))
                .thenReturn(new Utils.CompletedProcess("", 1, "", "Installation error"));

        IOException exception = assertThrows(IOException.class, () -> stage.updateAlternatives(log, javaHome));
        assertTrue(exception.getMessage().contains("Failed to register"));

        verify(runner).run(contains("--install"));
        verify(runner, never()).run(contains("--set"));
    }

    @Test
    void updateAlterativesWhenSetFailsShouldThrowExceptionTest() throws IOException, InterruptedException {
        String javaHome = "path/to/jdk17";
        String javaBinary = javaHome + "/bin/java";

        when(runner.run("update-alternatives --query java"))
                .thenReturn(new Utils.CompletedProcess("", 0, javaBinary + "\n", ""));
        when(runner.run("sudo update-alternatives --set java " + javaBinary))
                .thenReturn(new Utils.CompletedProcess("", 1, "", "Set error"));

        IOException exception = assertThrows(IOException.class, () -> stage.updateAlternatives(log, javaHome));
        assertTrue(exception.getMessage().contains("Failed to set"));

        verify(runner).run(contains("--set"));
    }
}