package be.unamur.snail.tool.energy;

public enum MonitoringType {
    CALLTREES, METHODS;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
