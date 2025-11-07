package be.unamur.snail.tool.energy;

public enum MeasurementType {
    RUNTIME, TOTAL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
