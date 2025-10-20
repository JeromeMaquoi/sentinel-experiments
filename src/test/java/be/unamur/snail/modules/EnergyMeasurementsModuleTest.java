package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class EnergyMeasurementsModuleTest {
    private EnergyMeasurementTool mockTool;
    private Stage mockSetupStage1;
    private Stage mockSetupStage2;
    private Stage mockCloneStage;
    private Stage mockMeasureStage1;
    private Stage mockMeasureStage2;
    private Stage mockPostStage;
    private Path tempDir;
    private Context context;

    @BeforeEach
    void setUp() throws Exception {
        mockSetupStage1 = mock(Stage.class);
        mockSetupStage2 = mock(Stage.class);
        mockCloneStage = mock(CloneAndCheckoutRepositoryStage.class);
        mockMeasureStage1 = mock(Stage.class);
        mockMeasureStage2 = mock(Stage.class);
        mockPostStage = mock(Stage.class);

        mockTool = mock(EnergyMeasurementTool.class);
        when(mockTool.createSetupStages()).thenReturn(List.of(mockSetupStage1, mockSetupStage2));
        when(mockTool.createMeasurementStages()).thenReturn(List.of(mockMeasureStage1, mockMeasureStage2));
        when(mockTool.createPostProcessingStages()).thenReturn(List.of(mockPostStage));

        EnergyMeasurementToolFactory factory = mock(EnergyMeasurementToolFactory.class);
        when(factory.create("joularjx")).thenReturn(mockTool);

        tempDir = Files.createTempDirectory("joular");
        Path yaml = tempDir.resolve("config.yml");
        Files.writeString(yaml, """
            execution-plan:
                num-test-runs: 2
                energy-measurements:
                    tool: "joularjx"
        """);
        Config.load(yaml.toString());

        context = new Context();
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    @Test
    void runShouldExecuteAllStagesInOrderTest() throws Exception {
        List<Stage> testStages = new ArrayList<>();
        testStages.add(mockCloneStage);
        testStages.addAll(mockTool.createSetupStages());
        for (int i = 0; i < 2; i++) {
            testStages.addAll(mockTool.createMeasurementStages());
        }
        testStages.addAll(mockTool.createPostProcessingStages());

        EnergyMeasurementsModule module = new EnergyMeasurementsModule(testStages);

        module.run(context);

        InOrder inOrder = inOrder(mockCloneStage, mockSetupStage1, mockSetupStage2, mockMeasureStage1, mockMeasureStage2, mockPostStage);
        inOrder.verify(mockCloneStage).execute(context);
        inOrder.verify(mockSetupStage1).execute(context);
        inOrder.verify(mockSetupStage2).execute(context);

        for (int i=0; i<2; i++) {
            inOrder.verify(mockMeasureStage1).execute(context);
            inOrder.verify(mockMeasureStage2).execute(context);
        }

        inOrder.verify(mockPostStage).execute(context);
    }
}