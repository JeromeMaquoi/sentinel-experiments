package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.utils.Utils;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class RunProjectTestsStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RunProjectTestsStage.class);

    private final InitScriptGenerator initScriptGenerator;

    public RunProjectTestsStage() {
        this.initScriptGenerator = new InitScriptGenerator();
    }

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String testCommand = config.getExecutionPlan().getTestCommand();
        String cwd = context.getRepoPath();

        // Add init script to see the logs in the terminal during the execution
        File initScript = initScriptGenerator.generateShowLogsInitScript();
        String commandWithInit = testCommand + " --init-script " + initScript.getAbsolutePath();

        Utils.CompletedProcess result = Utils.runCommand(commandWithInit, cwd);

        if (result.returnCode() != 0) {
            log.error("Project tests execution failed with code {}", result.returnCode());
            if (!config.getExecutionPlan().getIgnoreFailures()) {
                throw new TestSuiteExecutionFailedException();
            }
            log.warn("Ignoring failures, continuing anyway.");
        }
        log.info("Project tests execution completed");

        // Delete init script
        Files.deleteIfExists(initScript.toPath());
    }
}
