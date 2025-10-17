package be.unamur.snail.tool.energy;

public class EnergyMeasurementToolFactory {
    public EnergyMeasurementTool create(String toolName) {
        switch (toolName.toLowerCase()) {
            case "joularjx":
                return new JoularJXTool();
            default:
                throw new IllegalArgumentException("Unsupported energy measurement tool: " + toolName);
        }
    }
}
