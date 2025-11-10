package be.unamur.snail.tool.energy.model;

import be.unamur.snail.tool.energy.*;

public class BaseMeasurementDTO {
    private Scope scope;
    private MeasurementLevel measurementLevel;
    private MonitoringType monitoringType;
    private RunIterationDTO iteration;
    private CommitSimpleDTO commit;
    private Float value;

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public MeasurementLevel getMeasurementLevel() {
        return measurementLevel;
    }

    public void setMeasurementLevel(MeasurementLevel type) {
        this.measurementLevel = type;
    }

    public MonitoringType getMonitoringType() {
        return monitoringType;
    }

    public void setMonitoringType(MonitoringType monitoringType) {
        this.monitoringType = monitoringType;
    }

    public RunIterationDTO getIteration() {
        return iteration;
    }

    public void setIteration(RunIterationDTO iteration) {
        this.iteration = iteration;
    }

    public CommitSimpleDTO getCommit() {
        return commit;
    }

    public void setCommit(CommitSimpleDTO commit) {
        this.commit = commit;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BaseMeasurementDTO{" +
                "scope=" + scope +
                ", type=" + measurementLevel +
                ", monitoringType=" + monitoringType +
                ", iteration=" + iteration +
                ", commit=" + commit +
                ", value=" + value +
                '}';
    }
}
