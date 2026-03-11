package be.unamur.snail.tool.database;

public class ImportToolFactory {
    public ImportTool create(String toolName) {
        switch (toolName.toLowerCase()) {
            case "joularxjx":
                 return new JoularJXImportTool();
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + toolName);
        }
    }
}
