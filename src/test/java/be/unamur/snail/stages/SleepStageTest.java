package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SleepStageTest {
    private SleepStage stage;
    private Config mockConfig;
    private Config.ExecutionPlanConfig mockExecutionPlan;
    private Config.EnergyMeasurementConfig mockEnergyMeasurement;
    private Context mockContext;

    @BeforeEach
    void setUp() {
        mockConfig = mock(Config.class);
        mockExecutionPlan = mock(Config.ExecutionPlanConfig.class);
        mockEnergyMeasurement = mock(Config.EnergyMeasurementConfig.class);
        mockContext = mock(Context.class);
        mockContext.setLogger(new ConsolePipelineLogger(SleepStage.class));
        stage = new SleepStage(mockConfig);
    }

    @Test
    void executeMissingExecutionPlanTest() {
        when(mockConfig.getExecutionPlan()).thenReturn(null);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeMissingEnergyMeasurementTest() {
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(null);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeWithNegativeSleepDurationTest() {
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(mockEnergyMeasurement);
        when(mockEnergyMeasurement.getSleepDurationSeconds()).thenReturn(-5);
        when(mockContext.getLogger()).thenReturn(new ConsolePipelineLogger(SleepStage.class));

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue(elapsedTime < 100, "Execution with negative duration should complete quickly");
    }

    @Test
    void executeSuccessfullyWithSleepDurationTest() {
        when(mockConfig.getExecutionPlan()).thenReturn(mockExecutionPlan);
        when(mockExecutionPlan.getEnergyMeasurements()).thenReturn(mockEnergyMeasurement);
        when(mockEnergyMeasurement.getSleepDurationSeconds()).thenReturn(2);
        when(mockContext.getLogger()).thenReturn(new ConsolePipelineLogger(SleepStage.class));
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue(elapsedTime > 2000, "Sleep should last at least 2 seconds");
        assertTrue(elapsedTime < 2500, "Sleep should not last too long");
    }
}