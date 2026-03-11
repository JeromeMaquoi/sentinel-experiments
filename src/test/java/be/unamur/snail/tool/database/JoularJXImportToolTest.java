package be.unamur.snail.tool.database;

import be.unamur.snail.core.Config;
import be.unamur.snail.stages.ImportMeasurementsStage;
import be.unamur.snail.stages.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoularJXImportToolTest {
    @Test
    void createImportStagesReturnsImportStagesTest() {
        Config config =  mock(Config.class);
        Config.ProjectConfig projectConfig = mock(Config.ProjectConfig.class);
        when(projectConfig.getSubProject()).thenReturn("");

        when(config.getProject()).thenReturn(projectConfig);

        Config.ExecutionPlanConfig execPlan = mock(Config.ExecutionPlanConfig.class);
        Config.EnergyMeasurementConfig energy = mock(Config.EnergyMeasurementConfig.class);

        when(config.getExecutionPlan()).thenReturn(execPlan);
        when(execPlan.getEnergyMeasurements()).thenReturn(energy);

        JoularJXImportTool tool = new JoularJXImportTool(config);

        List<Stage> stages = tool.createImportStages();

        assertEquals(1, stages.size());
        assertInstanceOf(ImportMeasurementsStage.class, stages.get(0));
    }

    @Test
    void createCleanStagesReturnsCleanStagesTest() {
        Config config = mock(Config.class);
        JoularJXImportTool tool = new JoularJXImportTool(config);
        List<Stage> stages = tool.createCleanupStages();
        assertEquals(0, stages.size());
    }
}