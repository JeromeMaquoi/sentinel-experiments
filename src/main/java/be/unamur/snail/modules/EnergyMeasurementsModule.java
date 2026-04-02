package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.logging.ProgressBar;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.tool.energy.EnergyMeasurementTool;
import be.unamur.snail.tool.energy.EnergyMeasurementToolFactory;

import java.util.ArrayList;
import java.util.List;

public class EnergyMeasurementsModule extends AbstractModule {
    private final List<Stage> stages;
    // Number of stages before the first measurement iteration (clone + tool setup)
    private final int numSetupStages;
    // Number of stages in one measurement iteration
    private final int measurementStagesPerRun;
    // Total number of measurement iterations
    private final int numTestRuns;

    public EnergyMeasurementsModule() {
        EnergyMeasurementToolFactory factory = new EnergyMeasurementToolFactory();
        Config config = Config.getInstance();
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        EnergyMeasurementTool tool = factory.create(toolName);

        this.numTestRuns = config.getExecutionPlan().getNumTestRuns();

        List<Stage> setupStages = tool.createSetupStages();
        this.numSetupStages = 1 + setupStages.size(); // 1 for clone stage

        // First run is created separately so we can capture measurementStagesPerRun
        List<Stage> firstRun = tool.createMeasurementStages();
        this.measurementStagesPerRun = firstRun.size();

        List<Stage> allStages = new ArrayList<>();
        allStages.add(new CloneAndCheckoutRepositoryStage());
        allStages.addAll(setupStages);
        allStages.addAll(firstRun);
        for (int i = 1; i < numTestRuns; i++) {
            allStages.addAll(tool.createMeasurementStages());
        }
        allStages.addAll(tool.createPostProcessingStages());

        this.stages = allStages;
    }

    EnergyMeasurementsModule(List<Stage> stages) {
        this.stages = stages;
        this.numSetupStages = 0;
        this.measurementStagesPerRun = 0;
        this.numTestRuns = 0;
    }

    @Override
    protected List<Stage> getStages() {
        return stages;
    }

    /**
     * Overrides the default loop so that:
     * <ul>
     *   <li>The progress bar total equals {@code stages.size()} — covering setup,
     *       every individual measurement stage and post-processing — so the bar
     *       moves continuously throughout the entire pipeline.</li>
     *   <li>Each stage advances the counter by one and prefixes the stage name
     *       with an iteration label (e.g. {@code "Run 35/100 ▸ WarmupStage"}),
     *       giving both fine-grained progress and iteration context.</li>
     *   <li>Elapsed time is tracked from before the first setup stage through
     *       to the end of post-processing.</li>
     * </ul>
     */
    @Override
    public void run(Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        ProgressBar progressBar = context.getProgressBar();

        if (progressBar != null) {
            progressBar.start(stages.size());
        }

        try {
            int stageIndex = 0;
            for (Stage stage : stages) {
                String label = iterationLabel(stageIndex);
                if (progressBar != null) {
                    progressBar.setStageName(label + stage.getName()); // show name before executing
                }
                log.stageStart(stage.getName());
                stage.execute(context);
                log.stageEnd(stage.getName());
                if (progressBar != null) {
                    progressBar.advance(label + stage.getName()); // increment only after completion
                }
                stageIndex++;
            }
        } finally {
            if (progressBar != null) {
                progressBar.stop();
            }
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Returns a human-readable prefix for the stage at {@code stageIndex}:
     * {@code "Setup ▸ "}, {@code "Run X/Y ▸ "} or {@code "Post ▸ "}.
     * Returns an empty string when metadata is unavailable (test path).
     */
    private String iterationLabel(int stageIndex) {
        if (measurementStagesPerRun == 0 || numTestRuns == 0) return "";
        int measurementEnd = numSetupStages + numTestRuns * measurementStagesPerRun;
        if (stageIndex < numSetupStages) {
            return "Setup \u25b8 ";
        } else if (stageIndex < measurementEnd) {
            int currentRun = (stageIndex - numSetupStages) / measurementStagesPerRun + 1;
            return String.format("Run %d/%d \u25b8 ", currentRun, numTestRuns);
        } else {
            return "Post \u25b8 ";
        }
    }


    /**
     * Kept for backward compatibility with existing tests.
     * Production code builds stages directly in the constructor.
     */
    public static List<Stage> buildStagesFromConfig(EnergyMeasurementToolFactory factory, Config config) {
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        int numTestRuns = config.getExecutionPlan().getNumTestRuns();
        EnergyMeasurementTool tool = factory.create(toolName);

        List<Stage> allStages = new ArrayList<>();

        String repoDir = config.getProject().getName() + "_measurements_" + config.getRepo().getCommit();
        allStages.add(new CloneAndCheckoutRepositoryStage(repoDir));
        allStages.addAll(tool.createSetupStages());

        // Measurement stages repeated
        for (int i = 0; i < numTestRuns; i++) {
            allStages.addAll(tool.createMeasurementStages());
        }

        // Post-processing stages to run after the measurements
        allStages.addAll(tool.createPostProcessingStages());

        return allStages;
    }
}
