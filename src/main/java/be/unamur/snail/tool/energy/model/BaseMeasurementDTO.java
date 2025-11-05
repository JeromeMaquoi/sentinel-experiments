package be.unamur.snail.tool.energy.model;

import be.unamur.snail.tool.energy.*;

import java.util.List;

public class BaseMeasurementDTO {
    private Scope scope;
    private MeasurementType type;
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

    public BaseMeasurementDTO withScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public MeasurementType getType() {
        return type;
    }

    public void setType(MeasurementType type) {
        this.type = type;
    }

    public BaseMeasurementDTO withMeasurementType(MeasurementType type) {
        this.type = type;
        return this;
    }

    public MonitoringType getMonitoringType() {
        return monitoringType;
    }

    public void setMonitoringType(MonitoringType monitoringType) {
        this.monitoringType = monitoringType;
    }

    public BaseMeasurementDTO withMonitoringType(MonitoringType monitoringType) {
        this.monitoringType = monitoringType;
        return this;
    }

    public RunIterationDTO getIteration() {
        return iteration;
    }

    public void setIteration(RunIterationDTO iteration) {
        this.iteration = iteration;
    }

    public BaseMeasurementDTO withIteration(RunIterationDTO iteration) {
        this.iteration = iteration;
        return this;
    }

    public CommitSimpleDTO getCommit() {
        return commit;
    }

    public void setCommit(CommitSimpleDTO commit) {
        this.commit = commit;
    }

    public BaseMeasurementDTO withCommit(CommitSimpleDTO commit) {
        this.commit = commit;
        return this;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public BaseMeasurementDTO withValue(Float value) {
        this.value = value;
        return this;
    }
}
