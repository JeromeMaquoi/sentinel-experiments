package be.unamur.snail.tool;

public class ToolReleaseFetcherFactory {
    public ToolReleaseFetcher createFectcher(String toolName) {
        switch (toolName) {
            case "joularjx":
                return new JoularJXFetcher();
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + toolName);
        }
    }
}
