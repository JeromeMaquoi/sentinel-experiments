package be.unamur.snail;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.FilePipelineLogger;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.modules.EnergyMeasurementsModule;
import be.unamur.snail.modules.Module;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.modules.SpoonInstrumentConstructorModule;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: sentinel-experiments <module-type> --config <path>");
            System.exit(1);
        }
        String moduleArg = args[0];
        String configPath = args[2];

        // Load YAML config file
        Config.load(configPath);

        // Override log level from config file params
        Config config = Config.getInstance();
        System.setProperty("log.level", config.getLog().getLevel());

        Path logFilePath = Path.of(config.getLog().getDirectory(), "pipeline.log");
        PipelineLogger pipelineLogger = new FilePipelineLogger(logFilePath);

        // Select module based on CLI argument
        Module module;
        switch (moduleArg) {
            case "instrumentconstructor":
                module = new SpoonInstrumentConstructorModule();
                break;
            case "measure":
                module = new EnergyMeasurementsModule();
                break;
            default:
                throw new IllegalArgumentException("Unsupported module type: " + moduleArg); 
        }

        Context context = new Context();
        context.setLogger(pipelineLogger);

        try {
            module.run(context);
        } catch (ModuleException e) {
            pipelineLogger.error("Pipeline failed: ", e);
            System.exit(1);
        }
    }
}