package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.PipelineLogger;

import java.math.BigInteger;

public class WarmupStage implements Stage {
    private final Config config;

    public WarmupStage() {
        this(Config.getInstance());
    }

    public WarmupStage(Config config) {
        this.config = config;
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        if (config.getExecutionPlan() == null) {
            throw new MissingConfigKeyException("executionPlan");
        }

        Config.EnergyMeasurementConfig energyMeasurements = config.getExecutionPlan().getEnergyMeasurements();
        if (energyMeasurements == null) {
            throw new MissingConfigKeyException("executionPlan.energyMeasurements");
        }

        int warmupDurationSeconds = energyMeasurements.getWarmupDurationSeconds();
        log.info("Starting warmup stage with duration {} seconds", warmupDurationSeconds);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (warmupDurationSeconds * 1000L);

        int n = 5000;
        while (System.currentTimeMillis() < endTime) {
            computeFibonacci(n, endTime);
            n += 100;
        }

        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Warmup stage completed after {} seconds", elapsedSeconds);
    }

    /**
     * Computes the nth Fibonacci number
     * The computation will stop early if the endTime deadline is reached.
     *
     * @param n the position in the Fibonacci sequence
     * @param endTime the deadline timestamp in milliseconds; computation will stop if exceeded
     * @return the nth Fibonacci number as a BigInteger, or partial result if time limit reached
     */
    private BigInteger computeFibonacci(int n, long endTime) {
        if (n <= 0) {
            return BigInteger.ZERO;
        }
        if (n == 1 || n == 2) {
            return BigInteger.ONE;
        }

        BigInteger prev = BigInteger.ZERO;
        BigInteger curr = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            // Check time limit on every iteration for precise stopping
            if (System.currentTimeMillis() >= endTime) {
                return curr; // Stop computation immediately
            }
            BigInteger next = curr.add(prev);
            prev = curr;
            curr = next;
        }

        return curr;
    }
}
