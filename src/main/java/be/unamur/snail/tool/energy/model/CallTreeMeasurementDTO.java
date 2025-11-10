package be.unamur.snail.tool.energy.model;

import java.util.List;

public class CallTreeMeasurementDTO extends BaseMeasurementDTO {
    private List<String> callstack;

    public List<String> getCallstack() {
        return callstack;
    }

    public void setCallstack(List<String> callstack) {
        this.callstack = callstack;
    }

    @Override
    public String toString() {
        return "CallTreeMeasurementDTO{" +
                "callstack=" + callstack +
                '}';
    }
}


