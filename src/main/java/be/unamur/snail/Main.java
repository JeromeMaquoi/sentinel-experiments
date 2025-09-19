package be.unamur.snail;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Module;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.modules.SpoonInstrumentConstructorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Logger log = LoggerFactory.getLogger(Main.class);

        // Select module based on CLI argument
        Module module;
        switch (moduleArg) {
            case "instrumentconstructor":
                module = new SpoonInstrumentConstructorModule();
                break;
            default:
                throw new IllegalArgumentException("Unsupported module type: " + moduleArg); 
        }

        Context context = new Context();
        try {
            module.run(context);
        } catch (ModuleException e) {
            log.error("Pipeline failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}