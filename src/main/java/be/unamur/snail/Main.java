package be.unamur.snail;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: sentinel-experiments <module-type> --config <path>");
            System.exit(1);
        }
        String requestedModuleType = args[0].toLowerCase();
        String configPath = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--config") && i + 1 < args.length) {
                configPath = args[i + 1];
                break;
            }
        }

        if (configPath == null) {
            System.err.println("Error: missing config path");
            System.exit(1);
        }

        
    }
}