package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProjectTestsStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RunProjectTestsStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String testCommand = config.getExecutionPlan().getTestCommand();
        String cwd = context.getRepoPath();
        Utils.CompletedProcess result = Utils.runCommand(testCommand, cwd);

        if (result.returnCode() != 0) {
            log.error("Project tests execution failed with code {}", result.returnCode());
            if (!config.getExecutionPlan().getIgnoreFailures()) {
                throw new TestSuiteExecutionFailedException();
            }
            log.warn("Ignoring failures, continuing anyway.");
        }
        log.info("Project tests execution completed: {}", result.stdout());
    }
}
