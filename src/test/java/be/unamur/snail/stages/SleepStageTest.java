package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SleepStage class.
 * Tests verify that the sleep stage correctly pauses for the configured duration
 * without performing any I/O or database operations.
 */
class SleepStageTest {
    private SleepStage stage;
    private Config mockConfig;
    private Config.ExecutionPlanConfig mockExecutionPlan;
    private Config.EnergyMeasurementConfig mockEnergyMeasurements;
    private Context mockContext;

    @BeforeEach
    void setUp() {
        mockConfig = new Config();
        mockExecutionPlan = new Config.ExecutionPlanConfig();
        mockEnergyMeasurements = new Config.EnergyMeasurementConfig();
        mockContext = new Context();
        mockContext.setLogger(new ConsolePipelineLogger(SleepStage.class));
        stage = new SleepStage(mockConfig);
    }

    @Test
    void executeMissingExecutionPlanTest() {
        mockConfig.setExecutionPlanForTests(null);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeMissingEnergyMeasurementsTest() {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(null);

        assertThrows(MissingConfigKeyException.class, () -> stage.execute(mockContext));
    }

    @Test
    void executeSuccessfulSleepTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setSleepDurationSecondsForTests(1); // 1 second

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Verify that sleep took approximately the specified duration
        assertTrue(elapsedTime >= 1000, "Sleep should last at least 1 second");
        assertTrue(elapsedTime < 1200, "Sleep should stop within 1.2 seconds");
    }

    @Test
    void executeZeroDurationTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setSleepDurationSecondsForTests(0); // 0 seconds

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Execution should complete almost immediately when duration is 0
        assertTrue(elapsedTime < 100, "Execution with 0 seconds should complete quickly");
    }

    @Test
    void executeNegativeDurationTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setSleepDurationSecondsForTests(-5); // negative

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Execution should complete almost immediately when duration is negative
        assertTrue(elapsedTime < 100, "Execution with negative duration should complete quickly");
    }

    @Test
    void executeSleepMultipleSecondsTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setSleepDurationSecondsForTests(2); // 2 seconds

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertTrue(elapsedTime >= 2000, "Sleep should last at least 2 seconds");
        assertTrue(elapsedTime < 2200, "Sleep should stop within 2.2 seconds");
    }
}