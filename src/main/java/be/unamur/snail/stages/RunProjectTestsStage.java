package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProjectTestsStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RunProjectTestsStage.class);

    private final Config config;

    public RunProjectTestsStage() {
        this(Config.getInstance());
    }

    public RunProjectTestsStage(Config config) {
        this.config = config;
    }

    @Override
    public void execute(Context context) throws Exception {
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

        String cwd = context.getRepoPath();
        Utils.CompletedProcess result = Utils.runCommand(testCommand, cwd);

        if (result.returnCode() != 0) {
            throw new TestSuiteExecutionFailedException();
        }
        log.info("Test suite execution completed");
    }
}
