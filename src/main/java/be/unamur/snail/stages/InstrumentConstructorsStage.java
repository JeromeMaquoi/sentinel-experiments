package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;

import java.util.List;
import java.util.Map;

import be.unamur.snail.processors.ConstructorInstrumentationProcessor;
import spoon.Launcher;

public class InstrumentConstructorsStage implements Stage {
    private final String outputDir;

    public InstrumentConstructorsStage(Map<String, Object> params) {
        this.outputDir = (String) params.get("outputDir");
    }

    @Override
    public void execute(Context context) throws Exception {
        String projectPath = context.get("projectPath");

        Launcher launcher = new Launcher();
        launcher.addInputResource(projectPath);
        launcher.setSourceOutputDirectory(outputDir);
        List<String> classPath = context.get("classPath");
        launcher.getEnvironment().setSourceClasspath(classPath.toArray(new String[0]));
        launcher.addProcessor(new ConstructorInstrumentationProcessor());
        launcher.run();
    }
}
