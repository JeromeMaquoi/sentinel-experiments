package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.Utils;

public class RunProjectTestsStage implements Stage {
    private final Config config;

    public RunProjectTestsStage() {
        this(Config.getInstance());
    }

    public RunProjectTestsStage(Config config) {
        this.config = config;
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        if (context.getRepoPath() == null || context.getRepoPath().isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        if (config.getExecutionPlan() == null) {
            throw new MissingConfigKeyException("executionPlan");
        }
        String testCommand = config.getExecutionPlan().getTestCommand();
        if (testCommand == null || testCommand.isBlank()) {
            throw new MissingConfigKeyException("executionPlan.testCommand");
        }
        
        log.info("Executing test suite with command: {}", testCommand);
        String cwd = context.getRepoPath();
        Utils.CompletedProcess result = Utils.runCommand(testCommand, cwd);

        if (result.returnCode() != 0) {
            throw new TestSuiteExecutionFailedException();
        }
        log.info("Test suite execution completed");
    }
}
