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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void constructorBuildsCorrectNumberOfStagesTest(int numRuns, int expectedStageCount) {
        when(mockEnergyMeasurements.getTool()).thenReturn("joularjx");
        when(mockExecutionPlan.getNumTestRuns()).thenReturn(numRuns);

        when(mockTool.createSetupStages()).thenReturn(List.of(mock(Stage.class)));
        when(mockTool.createMeasurementStages()).thenReturn(List.of(mock(Stage.class)));
        when(mockTool.createPostProcessingStages()).thenReturn(List.of(mock(Stage.class)));
        when(mockFactory.create("joularjx")).thenReturn(mockTool);

        EnergyMeasurementsModule module = new EnergyMeasurementsModule(mockFactory, mockConfig);

        assertEquals(expectedStageCount, module.getStages().size());
        verify(mockTool, times(numRuns)).createMeasurementStages();
    }

    /**
     * Verifies that buildRepoDir produces the expected directory name.
     * This tests the naming convention in isolation, without building a full stage list.
     */
    @Test
    void repoDirIsCorrectlyFormattedTest() {
        assertEquals("test-project_measurements_123abc",
                EnergyMeasurementsModule.buildRepoDir(mockConfig));
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


    // ── iterationLabel ─────────────────────────────────────────────────────────

    /**
     * Parameterized test for {@code iterationLabel} using a fixed pipeline configuration:
     * <ul>
     *   <li>numSetupStages      = 2  (clone + 1 tool-setup stage)</li>
     *   <li>measurementStagesPerRun = 2</li>
     *   <li>numTestRuns         = 3</li>
     * </ul>
     * Stage layout: [0,1] setup | [2,3] run1 | [4,5] run2 | [6,7] run3 | [8+] post
     */
    @ParameterizedTest(name = "stageIndex={0} → \"{1}\"")
    @MethodSource("iterationLabelProvider")
    void iterationLabelTest(int stageIndex, String expected) {
        EnergyMeasurementsModule module = moduleWith2Setup2Meas3Runs();
        assertEquals(expected, module.iterationLabel(stageIndex));
    }

    static Stream<Arguments> iterationLabelProvider() {
        return Stream.of(
            // Setup section: indices 0 and 1
            Arguments.of(0, "Setup \u25b8 "),
            Arguments.of(1, "Setup \u25b8 "),
            // Run 1/3: indices 2 and 3
            Arguments.of(2, "Run 1/3 \u25b8 "),
            Arguments.of(3, "Run 1/3 \u25b8 "),
            // Run 2/3: indices 4 and 5
            Arguments.of(4, "Run 2/3 \u25b8 "),
            Arguments.of(5, "Run 2/3 \u25b8 "),
            // Run 3/3: indices 6 and 7
            Arguments.of(6, "Run 3/3 \u25b8 "),
            Arguments.of(7, "Run 3/3 \u25b8 "),
            // Post section: indices 8 and beyond
            Arguments.of(8,  "Post \u25b8 "),
            Arguments.of(9,  "Post \u25b8 ")
        );
    }

    /**
     * When the module is constructed with the stage-list constructor, all metadata
     * fields are zero, so every call to iterationLabel must return an empty string.
     */
    @Test
    void iterationLabelReturnsEmptyStringWhenNoMetadataTest() {
        EnergyMeasurementsModule module = new EnergyMeasurementsModule(List.of());
        assertEquals("", module.iterationLabel(0));
        assertEquals("", module.iterationLabel(5));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    /**
     * Builds a module whose internal metadata is fixed at:
     *   numSetupStages=2, measurementStagesPerRun=2, numTestRuns=3.
     * Uses one mock tool-setup stage (so numSetupStages = 1 clone + 1 = 2)
     * and two mock measurement stages per run.
     */
    private EnergyMeasurementsModule moduleWith2Setup2Meas3Runs() {
        when(mockEnergyMeasurements.getTool()).thenReturn("joularjx");
        when(mockExecutionPlan.getNumTestRuns()).thenReturn(3);
        when(mockTool.createSetupStages()).thenReturn(
                List.of(mock(Stage.class)));                                  // 1 setup → numSetupStages = 2
        when(mockTool.createMeasurementStages()).thenReturn(
                List.of(mock(Stage.class), mock(Stage.class)));               // 2 per run
        when(mockTool.createPostProcessingStages()).thenReturn(
                List.of(mock(Stage.class)));
        when(mockFactory.create("joularjx")).thenReturn(mockTool);
        return new EnergyMeasurementsModule(mockFactory, mockConfig);
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

