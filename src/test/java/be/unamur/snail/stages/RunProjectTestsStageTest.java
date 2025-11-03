package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RunProjectTestsStageTest {
    private RunProjectTestsStage stage;
    private Config mockConfig;
    private Config.ExecutionPlanConfig mockExecutionPlan;
    private Context mockContext;

    @BeforeEach
    void setUp() {
        mockConfig = mock(Config.class);
        mockExecutionPlan = mock(Config.ExecutionPlanConfig.class);
        mockContext = mock(Context.class);
        when(mockContext.getLogger()).thenReturn(new ConsolePipelineLogger(RunProjectTestsStage.class));
        stage = new RunProjectTestsStage(mockConfig);
    }

    @Test
    void executeMissingRepoPathTest() {
        when(mockContext.getRepoPath()).thenReturn(null);
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
        when(mockContext.getRepoPath()).thenReturn("");
        assertThrows(MissingContextKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeMissingExecutionPlanTest() {
        when(mockContext.getRepoPath()).thenReturn("repo");
        when(mockConfig.getExecutionPlan()).thenReturn(null);

        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeMissingTestCommandTest() {
        when(mockContext.getRepoPath()).thenReturn("repo");
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getTestCommand()).thenReturn(null);

        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));

        when(mockExecutionPlan.getTestCommand()).thenReturn("");
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeSucessfulExecutionTest() {
        when(mockContext.getRepoPath()).thenReturn("repo");
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getTestCommand()).thenReturn("mvn test");

        Utils.CompletedProcess process = new Utils.CompletedProcess("output", 0, "", "");

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.runCommand("mvn test", "repo")).thenReturn(process);
            assertDoesNotThrow(() -> stage.execute(mockContext));
            utils.verify(() -> Utils.runCommand("mvn test", "repo"), times(1));
        }
    }

    @Test
    void executeFailsWhenCommandReturnsNonZeroTest() {
        when(mockContext.getRepoPath()).thenReturn("repo");
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getTestCommand()).thenReturn("mvn test");

        Utils.CompletedProcess process = new Utils.CompletedProcess("output", 1, "", "");

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.runCommand("mvn test", "repo")).thenReturn(process);
            assertThrows(Exception.class, () -> stage.execute(mockContext));
            utils.verify(() -> Utils.runCommand("mvn test", "repo"), times(1));
        }
    }
}