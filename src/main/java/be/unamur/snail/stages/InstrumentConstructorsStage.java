package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;

import be.unamur.snail.processors.ConstructorInstrumentationProcessor;
import spoon.Launcher;

public class InstrumentConstructorsStage implements Stage {
    @Override
    public void execute(Context context) {
        Config config = Config.getInstance();
        String projectPath = config.getProject().getProjectPath();
        String inputPath = projectPath + "/src/main/java/";
        String outputDir = config.getOutputDir() + "/src/main/java/";

        Launcher launcher = new Launcher();
        launcher.addInputResource(inputPath);
        launcher.setSourceOutputDirectory(outputDir);
        String[] classPath = context.get("classPath");
        launcher.getEnvironment().setSourceClasspath(classPath);
        launcher.addProcessor(new ConstructorInstrumentationProcessor());
        launcher.run();
    }
}
