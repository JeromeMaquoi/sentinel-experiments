package be.unamur.snail.tool.energy;

import java.util.List;

public interface MeasurementSender {
    <T> void sendMeasurement(List<T> measurements, String endpoint);
}
