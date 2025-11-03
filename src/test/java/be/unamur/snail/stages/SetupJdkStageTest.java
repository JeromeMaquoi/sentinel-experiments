package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.jdk.JdkManager;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.utils.CommandRunner;
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

    @BeforeEach
    void setUp() {
        manager = mock(JdkManager.class);
        config = mock(Config.class);
        repo = mock(Config.RepoConfig.class);
        context = new Context();
        context.setLogger(new ConsolePipelineLogger(SetupJdkStage.class));
        runner = mock(CommandRunner.class);

        when(config.getRepo()).thenReturn(repo);
        stage = new SetupJdkStage(manager, config, runner);
    }

    @Test
    void executeWithExistingJdkTest() throws Exception {
        String jdkVersion = "17.0.17-tem";
        when(repo.getJdk()).thenReturn(jdkVersion);
        when(manager.isInstalled(jdkVersion)).thenReturn(true);
        when(manager.getJavaHome(jdkVersion)).thenReturn("/path/to/jdk17");

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
        when(repo.getJdk()).thenReturn(jdkVersion);
        when(manager.isInstalled(jdkVersion)).thenReturn(false);
        when(manager.getJavaHome(jdkVersion)).thenReturn("/path/to/jdk17");

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
}