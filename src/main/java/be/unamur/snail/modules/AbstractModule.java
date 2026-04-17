package be.unamur.snail.modules;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.logging.ProgressBar;
import be.unamur.snail.stages.Stage;
import java.util.List;
/**
 * Base class for pipeline modules.
 * Provides the common stage-iteration loop and optional progress-bar
 * integration: if a {@link ProgressBar} is present on the {@link Context}
 * it is started, the stage name is shown before each stage executes, the
 * counter is advanced after each stage completes, and the bar is stopped
 * once all stages have finished or an exception is thrown.
 */
public abstract class AbstractModule implements Module {
    protected abstract List<Stage> getStages();

    @Override
    public void run(Context context) throws Exception {
        List<Stage> stages = getStages();
        PipelineLogger log = context.getLogger();
        ProgressBar progressBar = context.getProgressBar();

        if (progressBar != null) {
            progressBar.setLogger(log);
            progressBar.start(stages.size());
        }

        try {
            for (Stage stage : stages) {
                if (progressBar != null) {
                    progressBar.setStageName(stage.getName());
                }
                log.stageStart(stage.getName());
                stage.execute(context);
                log.stageEnd(stage.getName());
                if (progressBar != null) {
                    progressBar.advance(stage.getName());
                }
            }
        } finally {
            if (progressBar != null) {
                progressBar.stop();
            }
        }
    }
}
