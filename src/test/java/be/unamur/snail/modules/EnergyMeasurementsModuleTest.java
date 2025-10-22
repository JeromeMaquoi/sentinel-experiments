package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(mockEnergyMeasurements);
    }

    @Test
    void buildStagesFromConfigWithMultipleRunsTest() {
        when(mockEnergyMeasurements.getTool()).thenReturn("joularjx");
        when(mockExecutionPlan.getNumTestRuns()).thenReturn(2);

        List<Stage> setupStages = List.of(mock(Stage.class));
        List<Stage> measurementStages = List.of(mock(Stage.class));
        List<Stage> postStages = List.of(mock(Stage.class));

        when(mockTool.createSetupStages()).thenReturn(setupStages);
        when(mockTool.createMeasurementStages()).thenReturn(measurementStages);
        when(mockTool.createPostProcessingStages()).thenReturn(postStages);

        when(mockFactory.create("joularjx")).thenReturn(mockTool);

        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(mockEnergyMeasurements);

        List<Stage> stages = EnergyMeasurementsModule.buildStagesFromConfig(mockFactory, mockConfig);
        // Expected: 1 setup + 2 measurements + 1 post + 1 CloneAndCheckoutRepositoryStage
        assertEquals(5, stages.size());
    }
}