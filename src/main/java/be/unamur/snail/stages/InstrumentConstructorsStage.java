package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;

import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.processors.ConstructorInstrumentationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;

import java.util.List;

public class InstrumentConstructorsStage implements Stage {
    private static Logger log = LoggerFactory.getLogger(InstrumentConstructorsStage.class);
    @Override
    public void execute(Context context) throws ModuleException {
        Config config = Config.getInstance();
        String projectPath = config.getRepo().getTargetDir() + "_" + config.getRepo().getCommit();
        String subProject = config.getProject().getSubProject();
        String sourceCodePath = projectPath + subProject + "/src/main/java/";
        log.info("Starting instrumentation process for {}", sourceCodePath);

        try {
            Launcher launcher = new Launcher();
            launcher.addInputResource(sourceCodePath);
            launcher.setSourceOutputDirectory(sourceCodePath);
            List<String> classPaths = context.get("classPath");
            launcher.getEnvironment().setSourceClasspath(classPaths.toArray(new String[0]));
            launcher.addProcessor(new ConstructorInstrumentationProcessor());
            launcher.run();
            log.info("Instrumentation completed.");
        } catch (Exception e) {
            log.error("Failed to instrument constructors for project {}", projectPath, e);
            throw new ModuleException("Instrumentation stage failed", e);
        }
    }
}
