package be.unamur.snail;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Module;
import be.unamur.snail.modules.SpoonInstrumentConstructorModule;

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

        // Select module based on CLI argument
        Module module;
        Object config;
        switch (moduleArg) {
            case "instrumentconstructor":
                module = new SpoonInstrumentConstructorModule();
                break;
            default:
                throw new IllegalArgumentException("Unsupported module type: " + moduleArg);
        }

        Context context = new Context();
        module.run(context);
    }
}