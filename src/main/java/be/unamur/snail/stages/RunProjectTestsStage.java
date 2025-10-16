package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.utils.Utils;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

/**
 * Stage made to execute the test suite of the analyzed project. It passes some
 * properties to the tests:
 * - packagePrefix: the package prefix used by the instrumented code to filter the
 * stacktraces
 * - apiUrl: the complete apiUrl used by the instrumented code to post data to the db
 * PRE: this stages needs the module CkModule to be run, as this module will add Ck data
 * into the db, data needed by the instrumented code in this stage to add constructor
 * context data into the db
 */
public class RunProjectTestsStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RunProjectTestsStage.class);

    private final InitScriptGenerator initScriptGenerator;

    public RunProjectTestsStage() {
        this.initScriptGenerator = new InitScriptGenerator();
    }

    @Override
    public void execute(Context context) throws Exception {
        if (context.getRepoPath() == null || context.getRepoPath().isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        Config config = Config.getInstance();
        String testCommand = config.getExecutionPlan().getCommand();
        String cwd = context.getRepoPath();

        // Add init script to see the logs in the terminal during the execution and for passing properties packagePrefix and apiUrl to the tests
        File initScript = initScriptGenerator.generateShowLogsInitScriptForGradle();
        // TODO handle Maven and not only Gradle
        String commandWithInit = testCommand + " --init-script " + initScript.getAbsolutePath();

        // Add package prefix as system property for stacktrace filtering
        String packagePrefix = config.getProject().getPackagePrefix();
        if (packagePrefix != null && !packagePrefix.isBlank()) {
            commandWithInit += " -DpackagePrefix=" + packagePrefix;
        }

        // Add API endpoint as system property for data sending to the db
        String endpoint = config.getBackend().getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            String host = config.getBackend().getServerHost();
            int port = config.getBackend().getServerPort();
            String completeEndpoint = "http://" + host + ":" + port + endpoint;
            log.info("Setting endpoint to {}", completeEndpoint);
            commandWithInit += " -DapiUrl=" + completeEndpoint;
        }
        log.info("Executing command {}", commandWithInit);
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
