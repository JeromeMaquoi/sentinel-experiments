package be.unamur.snail.tool.energy.model;

public class RuntimeMeasurementDTO extends BaseMeasurementDTO {
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
