package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;

import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.processors.ConstructorInstrumentationProcessor;
import spoon.Launcher;
import spoon.SpoonException;

import java.util.List;


/**
 * Stage responsible for the instrumentation of a project to get data
 * about the constructors of this project
 */
public class InstrumentConstructorsStage implements Stage {
    @Override
    public void execute(Context context) throws ModuleException {
        PipelineLogger log = context.getLogger();

        if (context.getClassPath() == null || context.getClassPath().isEmpty()) {
            throw new MissingContextKeyException("classPath");
        }

        Config config = Config.getInstance();
        String repoPath = context.getRepoPath();
        String subProject = config.getProject().getSubProject();
        String sourceCodePath = repoPath + subProject + "/src/main/java/";
        log.info("Starting instrumentation process for {}", sourceCodePath);

        try {
            Launcher launcher = new Launcher();
            launcher.addInputResource(sourceCodePath);
            launcher.setSourceOutputDirectory(sourceCodePath);
            List<String> classPaths = context.getClassPath();
            launcher.getEnvironment().setSourceClasspath(classPaths.toArray(new String[0]));
            launcher.addProcessor(new ConstructorInstrumentationProcessor());
            launcher.run();
            log.info("Instrumentation completed.");
        } catch (SpoonException e) {
            System.out.println(e);
            log.error("Failed to instrument constructors for project {}", repoPath, e);
            if (!config.getExecutionPlan().getIgnoreSpoonFailures()) {
                throw new ModuleException("Failed to instrument constructors for project " + repoPath, e);
            }
            log.warn("Ignoring failures, continuing anyway.");
        }
    }
}
