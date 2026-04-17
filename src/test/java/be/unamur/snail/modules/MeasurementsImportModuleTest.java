package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.database.ImportTool;
import be.unamur.snail.tool.database.ImportToolFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MeasurementsImportModuleTest {
    private Config config;
    private ImportToolFactory factory;
    private ImportTool tool;
    private Stage stage1;
    private Stage stage2;
    private Context context;
    private PipelineLogger logger;

    @BeforeEach
    void setUp() {
        config = mock(Config.class);
        factory = mock(ImportToolFactory.class);
        tool = mock(ImportTool.class);
        stage1 = mock(Stage.class);
        stage2 = mock(Stage.class);
        context = mock(Context.class);
        logger = mock(PipelineLogger.class);

        when(context.getLogger()).thenReturn(logger);

        when(tool.createPreparationStages()).thenReturn(List.of(stage1));
        when(tool.createImportStages()).thenReturn(List.of(stage2));
        when(tool.createCleanupStages()).thenReturn(List.of());

        when(factory.create(anyString())).thenReturn(tool);

        when(config.getExecutionPlan()).thenReturn(mock(Config.ExecutionPlanConfig.class));
        Config.ExecutionPlanConfig executionPlan = config.getExecutionPlan();
        Config.EnergyMeasurementConfig energyConfig = mock(Config.EnergyMeasurementConfig.class);
        when(executionPlan.getEnergyMeasurements()).thenReturn(energyConfig);
        when(energyConfig.getTool()).thenReturn("JoularJX");
    }

    @Test
    void buildRepoDirReturnsImportNameWithCommitTest() {
        Config config = mock(Config.class);
        Config.ProjectConfig project = mock(Config.ProjectConfig.class);
        Config.RepoConfig repo = mock(Config.RepoConfig.class);
        when(config.getProject()).thenReturn(project);
        when(config.getRepo()).thenReturn(repo);
        when(project.getName()).thenReturn("checkstyle");
        when(repo.getCommit()).thenReturn("abc123");

        assertEquals("checkstyle_import_abc123", MeasurementsImportModule.buildRepoDir(config));
    }

    @Test
    void buildStagesFromConfigReturnsAllStagesTest() {
        List<Stage> stages = MeasurementsImportModule.buildStagesFromConfig(factory, config);

        assertEquals(2, stages.size());
        assertSame(stage1, stages.get(0));
        assertSame(stage2, stages.get(1));

        verify(factory).create("JoularJX");
        verify(tool).createPreparationStages();
        verify(tool).createImportStages();
        verify(tool).createCleanupStages();
    }

    @Test
    void runExecutesAllStagesInOrderTest() throws Exception {
        MeasurementsImportModule module = new MeasurementsImportModule(List.of(stage1, stage2));
        module.run(context);

        InOrder inOrder = inOrder(logger, stage1, stage2);
        inOrder.verify(logger).stageStart(stage1.getName());
        inOrder.verify(stage1).execute(context);
        inOrder.verify(logger).stageEnd(stage1.getName());

        inOrder.verify(logger).stageStart(stage2.getName());
        inOrder.verify(stage2).execute(context);
        inOrder.verify(logger).stageEnd(stage2.getName());
    }
}