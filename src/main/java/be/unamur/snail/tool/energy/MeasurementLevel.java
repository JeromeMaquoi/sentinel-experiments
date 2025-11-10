package be.unamur.snail.tool.energy;

public enum MeasurementLevel {
    RUNTIME, TOTAL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
