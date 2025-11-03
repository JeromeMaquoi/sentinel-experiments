package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RunInstrumentedProjectTestsStageTest {
    private RunInstrumentedProjectTestsStage stage;
    private Context context;
    private Config config;
    private Config.ExecutionPlanConfig executionPlanConfig;

    @BeforeEach
    void setUp() {
        stage = new RunInstrumentedProjectTestsStage();

        context = mock(Context.class);
        when(context.getRepoPath()).thenReturn("fake-repo");
        when(context.getLogger()).thenReturn(new ConsolePipelineLogger(RunInstrumentedProjectTestsStage.class));

        config = mock(Config.class);
        executionPlanConfig = mock(Config.ExecutionPlanConfig.class);
        when(config.getExecutionPlan()).thenReturn(executionPlanConfig);
    }

    @Test
    void executedSuccessfulTest() {
        when(executionPlanConfig.getTestCommand()).thenReturn("echo OK");
        when(executionPlanConfig.getIgnoreFailures()).thenReturn(false);

        Config.ProjectConfig projectConfig = mock(Config.ProjectConfig.class);
        when(config.getProject()).thenReturn(projectConfig);
        when(projectConfig.getPackagePrefix()).thenReturn("be");

        Config.BackendConfig backendConfig = mock(Config.BackendConfig.class);
        when(config.getBackend()).thenReturn(backendConfig);
        when(backendConfig.getServerPort()).thenReturn(8080);
        when(backendConfig.getServerHost()).thenReturn("localhost");

        try (MockedStatic<Utils> utilsMock = Mockito.mockStatic(Utils.class)) {
            Utils.CompletedProcess mockProcess = new Utils.CompletedProcess("echo OK", 0, "OK", "");
            utilsMock.when(() -> Utils.runCommand(anyString(), anyString())).thenReturn(mockProcess);

            try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
                configMock.when(Config::getInstance).thenReturn(config);
                assertDoesNotThrow(() -> stage.execute(context));
            }
        }
    }

    @Test
    void executedFailedWithoutIgnoreFailuresTest() {
        when(executionPlanConfig.getTestCommand()).thenReturn("failCommand");
        when(executionPlanConfig.getIgnoreFailures()).thenReturn(false);

        Config.ProjectConfig projectConfig = mock(Config.ProjectConfig.class);
        when(config.getProject()).thenReturn(projectConfig);
        when(projectConfig.getPackagePrefix()).thenReturn("be");

        Config.BackendConfig backendConfig = mock(Config.BackendConfig.class);
        when(config.getBackend()).thenReturn(backendConfig);
        when(backendConfig.getServerPort()).thenReturn(8080);
        when(backendConfig.getServerHost()).thenReturn("localhost");

        try (MockedStatic<Utils> utilsMock = Mockito.mockStatic(Utils.class)) {
            Utils.CompletedProcess mockProcess = new Utils.CompletedProcess("failCommand", 1, "", "error");
            utilsMock.when(() -> Utils.runCommand(anyString(), anyString())).thenReturn(mockProcess);
            try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class)) {
                configMock.when(Config::getInstance).thenReturn(config);
                assertThrows(TestSuiteExecutionFailedException.class, () -> stage.execute(context));
            }
        }
    }

    @Test
    void executedFailedWithIgnoreFailuresTest() {
        when(executionPlanConfig.getTestCommand()).thenReturn("failCommand");
        when(executionPlanConfig.getIgnoreFailures()).thenReturn(true);

        Config.ProjectConfig projectConfig = mock(Config.ProjectConfig.class);
        when(config.getProject()).thenReturn(projectConfig);
        when(projectConfig.getPackagePrefix()).thenReturn("be");

        Config.BackendConfig backendConfig = mock(Config.BackendConfig.class);
        when(config.getBackend()).thenReturn(backendConfig);
        when(backendConfig.getServerPort()).thenReturn(8080);
        when(backendConfig.getServerHost()).thenReturn("localhost");

        try (MockedStatic<Utils> utilsMock = Mockito.mockStatic(Utils.class)) {
            Utils.CompletedProcess mockProcess = new Utils.CompletedProcess("failCommand", 1, "", "error");
            utilsMock.when(() -> Utils.runCommand(anyString(), anyString())).thenReturn(mockProcess);
            try (MockedStatic<Config> configMock = Mockito.mockStatic(Config.class)) {
                configMock.when(Config::getInstance).thenReturn(config);
                assertDoesNotThrow(() -> stage.execute(context));
            }
        }
    }

    @Test
    void executeThrowsMissingContextKeyTest() {
        when(context.getRepoPath()).thenReturn(null);
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeThrowsBlanckContextKeyTest() {
        when(context.getRepoPath()).thenReturn("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }
}