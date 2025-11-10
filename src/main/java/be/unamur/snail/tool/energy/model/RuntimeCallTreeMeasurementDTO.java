package be.unamur.snail.tool.energy.model;

import java.util.List;

public class RuntimeCallTreeMeasurementDTO extends RuntimeMeasurementDTO {
    private List<String> callstack;

    public List<String> getCallstack() {
        return callstack;
    }

    public void setCallstack(List<String> callstack) {
        this.callstack = callstack;
    }
}
