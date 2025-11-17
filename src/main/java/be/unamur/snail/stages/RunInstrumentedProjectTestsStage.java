package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.TestSuiteExecutionFailedException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.ProjectTypeDetector;
import be.unamur.snail.utils.Utils;
import be.unamur.snail.utils.gradle.InitScriptGenerator;

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
public class RunInstrumentedProjectTestsStage implements Stage {
    private final InitScriptGenerator initScriptGenerator;
    private final ProjectTypeDetector projectTypeDetector;

    public RunInstrumentedProjectTestsStage() {
        this.initScriptGenerator = new InitScriptGenerator();
        this.projectTypeDetector = new ProjectTypeDetector();
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        if (context.getRepoPath() == null || context.getRepoPath().isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        Config config = Config.getInstance();
        String testCommand = config.getExecutionPlan().getTestCommand();
        String cwd = context.getRepoPath();

        File repoPath = new File(context.getRepoPath());
        String commandWithInit = testCommand;
        File initScript = initScriptGenerator.generateShowLogsInitScriptForGradle();

        if (projectTypeDetector.isMavenProject(repoPath)) {
            // TODO
        } else if (projectTypeDetector.isGradleProject(repoPath)) {
            // Add init script to see the logs in the terminal during the execution and for passing properties packagePrefix and apiUrl to the tests
            commandWithInit += " --init-script " + initScript.getAbsolutePath();
            log.info("Using Gradle init script at {}", initScript.getAbsolutePath());
        }

        // Add package prefix as system property for stacktrace filtering
        String packagePrefix = config.getProject().getPackagePrefix();
        if (packagePrefix != null && !packagePrefix.isBlank()) {
            commandWithInit += " -DpackagePrefix=" + packagePrefix;
        }

        // Add API endpoint as system property for data sending to the db
        String endpoint = config.getBackend().getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            String completeEndpoint = Utils.createEndpointURL(config, endpoint);
            log.info("Setting endpoint to {}", completeEndpoint);
            commandWithInit += " -DapiUrl=" + completeEndpoint;
        }
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
