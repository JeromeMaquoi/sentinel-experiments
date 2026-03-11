package be.unamur.snail.tool.database;

import be.unamur.snail.core.Config;

public class ImportToolFactory {
    private final Config config;

    public ImportToolFactory(Config config) {
        this.config = config;
    }

    public ImportTool create(String toolName) {
        switch (toolName.toLowerCase()) {
            case "joularjx":
                 return new JoularJXImportTool(config);
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + toolName);
        }
    }
}
