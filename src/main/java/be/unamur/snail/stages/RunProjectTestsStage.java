package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RunProjectTestsStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RunProjectTestsStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String testCommand = config.getExecutionPlan().getTestCommand();
        String cwd = context.getRepoPath();

        // Add init script to see the logs in the terminal during the execution
        Path initScript = createInitScript();
        String commandWithInit = testCommand + " --init-script " + initScript.toAbsolutePath();

        Utils.CompletedProcess result = Utils.runCommand(commandWithInit, cwd);

        if (result.returnCode() != 0) {
            log.error("Project tests execution failed with code {}", result.returnCode());
            if (!config.getExecutionPlan().getIgnoreFailures()) {
                throw new TestSuiteExecutionFailedException();
            }
            log.warn("Ignoring failures, continuing anyway.");
        }
        log.info("Project tests execution completed");

        Files.deleteIfExists(initScript);
    }

    /**
     * Create a temp gradle file with init script in it to configure
     * the project to show logs in terminal during execution
     * @return a Path of the temporary file where the script has been
     * created
     * @throws IOException if there is a problem during the creation of
     * the file
     */
    public Path createInitScript() throws IOException {
        String script = """
                allprojects {
                    tasks.withType(Test).configureEach {
                        testLogging {
                            showStandardStreams = true
                            events 'passed', 'failed', 'skipped'
                            exceptionFormat 'full'
                        }
                    }
                }
                """;
        Path tempFile = Files.createTempFile("test-logging", ".gradle");
        Files.writeString(tempFile, script);
        return tempFile;
    }
}
