package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarmupStageTest {
    private WarmupStage stage;
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
        mockContext.setLogger(new ConsolePipelineLogger(WarmupStage.class));
        stage = new WarmupStage(mockConfig);
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
    void executeSuccessfulWarmupShortDurationTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setWarmupDurationSecondsForTests(1); // 1 second

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Verify that execution took approximately the specified duration (with some tolerance)
        // Allow 500ms tolerance for test execution overhead
        assertTrue(elapsedTime >= 1000, "Execution should take at least 1 second");
        assertTrue(elapsedTime < 1500, "Execution should stop within 1.5 seconds");
    }

    @Test
    void executeZeroDurationTest() {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setWarmupDurationSecondsForTests(0);

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Execution should complete almost immediately
        assertTrue(elapsedTime < 500, "Execution with 0 seconds should complete quickly");
    }

    @Test
    void executeStopsAtExactTimeTest() throws Exception {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setWarmupDurationSecondsForTests(1);

        long startTime = System.currentTimeMillis();
        stage.execute(mockContext);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // The execution should not significantly overshoot the target duration
        // With every-iteration time checking, it should be very close to the target
        assertTrue(elapsedTime <= 1100, "Should not exceed target duration by more than 100ms");
    }

    @Test
    void executeWithPositiveDurationTest() {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setWarmupDurationSecondsForTests(5);

        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> stage.execute(mockContext));
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Verify reasonable duration
        assertTrue(elapsedTime >= 5000, "Should run for at least 5 seconds");
        assertTrue(elapsedTime < 5500, "Should stop within 5.5 seconds");
    }

    @Test
    void executeMultipleTimesTest() {
        mockConfig.setExecutionPlanForTests(mockExecutionPlan);
        mockExecutionPlan.setEnergyMeasurementsForTests(mockEnergyMeasurements);
        mockEnergyMeasurements.setWarmupDurationSecondsForTests(1); // 1 second

        // Execute multiple times to verify consistency
        for (int i = 0; i < 3; i++) {
            long startTime = System.currentTimeMillis();
            assertDoesNotThrow(() -> stage.execute(mockContext));
            long elapsedTime = System.currentTimeMillis() - startTime;

            assertTrue(elapsedTime >= 1000, "Execution " + i + " should take at least 1 second");
            assertTrue(elapsedTime < 1500, "Execution " + i + " should stop within 1.5 seconds");
        }
    }
}

