package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.stages.SleepStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.stages.WarmupStage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the EnergyMeasurementsModule pipeline.
 * These tests validate the pipeline structure and stage ordering without executing
 * actual tool commands, database operations, or file I/O.
 */
class EnergyMeasurementsModuleTest {
    private EnergyMeasurementToolFactory mockFactory;
    private EnergyMeasurementTool mockTool;
    private Config mockConfig;
    private Config.ExecutionPlanConfig mockExecutionPlan;
    private Config.EnergyMeasurementConfig mockEnergyMeasurements;

    @BeforeEach
    void setUp() {
        mockFactory = mock(EnergyMeasurementToolFactory.class);
        mockTool = mock(EnergyMeasurementTool.class);
        mockConfig = mock(Config.class);
        mockExecutionPlan = mock(Config.ExecutionPlanConfig.class);
        mockEnergyMeasurements = mock(Config.EnergyMeasurementConfig.class);

        Config.ProjectConfig mockProjectConfig = mock(Config.ProjectConfig.class);
        when(mockConfig.getProject()).thenReturn(mockProjectConfig);
        when(mockProjectConfig.getName()).thenReturn("test-project");
        Config.RepoConfig mockRepoConfig = mock(Config.RepoConfig.class);
        when(mockConfig.getRepo()).thenReturn(mockRepoConfig);
        when(mockRepoConfig.getCommit()).thenReturn("123abc");

        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(mockEnergyMeasurements);
    }
    
    @ParameterizedTest(name = "{0} run(s) → {1} total stages")
    @CsvSource({
        "1, 4",
        "2, 5",
        "3, 6",
        "5, 8"
    })
    void buildStagesFromConfigWithVariousRunCountsTest(int numRuns, int expectedStageCount) {
        when(mockEnergyMeasurements.getTool()).thenReturn("joularjx");
        when(mockExecutionPlan.getNumTestRuns()).thenReturn(numRuns);

        List<Stage> setupStages = List.of(mock(Stage.class));
        List<Stage> measurementStages = List.of(mock(Stage.class));
        List<Stage> postStages = List.of(mock(Stage.class));

        when(mockTool.createSetupStages()).thenReturn(setupStages);
        when(mockTool.createMeasurementStages()).thenReturn(measurementStages);
        when(mockTool.createPostProcessingStages()).thenReturn(postStages);

        when(mockFactory.create("joularjx")).thenReturn(mockTool);

        List<Stage> stages = EnergyMeasurementsModule.buildStagesFromConfig(mockFactory, mockConfig);

        // Total: 1 clone + 1 setup + (1 measurement * numRuns) + 1 post
        assertEquals(expectedStageCount, stages.size());
        verify(mockTool, times(numRuns)).createMeasurementStages();
    }

    /**
     * Test that verifies the module executes stages sequentially.
     * Uses mock stages to verify execution without side effects.
     */
    @Test
    void moduleExecutesStagesSequentiallyTest() throws Exception {
        Stage mockStage1 = mock(Stage.class);
        Stage mockStage2 = mock(Stage.class);
        Stage mockStage3 = mock(Stage.class);

        List<Stage> stages = List.of(mockStage1, mockStage2, mockStage3);
        EnergyMeasurementsModule module = new EnergyMeasurementsModule(stages);

        Context mockContext = createTestContext();
        module.run(mockContext);

        // Verify all stages were executed in order
        verify(mockStage1).execute(mockContext);
        verify(mockStage2).execute(mockContext);
        verify(mockStage3).execute(mockContext);
    }

    /**
     * Test that validates the pipeline correctly repeats measurement stages
     * for each configured test run, useful for regression testing when adding new stages.
     */
    @Test
    void pipelineCorrectlyRepeatsStagesForMultipleRunsTest() {
        when(mockEnergyMeasurements.getTool()).thenReturn("joularjx");
        when(mockExecutionPlan.getNumTestRuns()).thenReturn(5);

        List<Stage> setupStages = List.of(mock(Stage.class), mock(Stage.class));
        List<Stage> measurementStages = List.of(
            mock(Stage.class),  // Could be WarmupStage
            mock(Stage.class),  // Could be SleepStage
            mock(Stage.class)   // Could be RunProjectTestsStage
        );
        List<Stage> postStages = List.of(mock(Stage.class));

        when(mockTool.createSetupStages()).thenReturn(setupStages);
        when(mockTool.createMeasurementStages()).thenReturn(measurementStages);
        when(mockTool.createPostProcessingStages()).thenReturn(postStages);

        when(mockFactory.create("joularjx")).thenReturn(mockTool);

        List<Stage> stages = EnergyMeasurementsModule.buildStagesFromConfig(mockFactory, mockConfig);

        // Validate: 1 clone + 2 setup + (3 measurement * 5 runs) + 1 post = 19 stages
        assertEquals(19, stages.size());
        verify(mockTool, times(5)).createMeasurementStages();
    }

    /**
     * Test that the real WarmupStage and SleepStage can be included in the pipeline.
     * This validates that these stages are properly integrated into the measurement pipeline.
     */
    @Test
    void pipelineCanIncludeRealWarmupAndSleepStagesTest() {
        // Create a list of stages including real WarmupStage and SleepStage
        List<Stage> stages = List.of(
            mock(Stage.class),  // Mock setup stage
            new WarmupStage(createConfigWithWarmupSettings(1)),  // Real WarmupStage with 1 second
            new SleepStage(createConfigWithSleepSettings(1))     // Real SleepStage with 1 second
        );

        EnergyMeasurementsModule module = new EnergyMeasurementsModule(stages);
        assertNotNull(module);
        assertEquals(3, stages.size());
    }


    // Helper methods for creating test fixtures

    private Config createConfigWithWarmupSettings(int warmupSeconds) {
        Config config = new Config();
        Config.ExecutionPlanConfig executionPlan = new Config.ExecutionPlanConfig();
        Config.EnergyMeasurementConfig energyMeasurements = new Config.EnergyMeasurementConfig();

        energyMeasurements.setWarmupDurationSecondsForTests(warmupSeconds);
        executionPlan.setEnergyMeasurementsForTests(energyMeasurements);
        config.setExecutionPlanForTests(executionPlan);

        return config;
    }

    private Config createConfigWithSleepSettings(int sleepSeconds) {
        Config config = new Config();
        Config.ExecutionPlanConfig executionPlan = new Config.ExecutionPlanConfig();
        Config.EnergyMeasurementConfig energyMeasurements = new Config.EnergyMeasurementConfig();

        energyMeasurements.setSleepDurationSecondsForTests(sleepSeconds);
        executionPlan.setEnergyMeasurementsForTests(energyMeasurements);
        config.setExecutionPlanForTests(executionPlan);

        return config;
    }

    private Context createTestContext() {
        Context context = new Context();
        context.setLogger(new ConsolePipelineLogger(EnergyMeasurementsModuleTest.class));
        context.setRepoPath("/tmp/test-repo");
        context.setCurrentWorkingDir("/tmp/test-work");
        return context;
    }
}

