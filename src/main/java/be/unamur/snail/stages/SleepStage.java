package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.logging.PipelineLogger;

public class SleepStage implements Stage{
    private final Config config;

    public SleepStage() {
        this(Config.getInstance());
    }

    public SleepStage(Config config) {
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

        int sleepDurationSeconds = energyMeasurements.getSleepDurationSeconds();
        if (sleepDurationSeconds <= 0) {
            log.warn("Sleep duration is 0 or negative, skipping sleep stage");
            return;
        }

        log.info("Sleeping for {} seconds to allow system to stabilize before measurements", sleepDurationSeconds);

        long startTime = System.currentTimeMillis();
        Thread.sleep(sleepDurationSeconds * 1000L);
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L;

        log.info("Sleep stage completed after {} seconds", elapsedSeconds);
    }
}
