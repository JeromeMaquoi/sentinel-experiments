package be.unamur.snail.tool.energy.model;

public class RunIterationDTO {
    private Integer pid;
    private long startTimestamp;

    public RunIterationDTO() { }

    public RunIterationDTO(Integer pid, long startTimestamp) {
        this.pid = pid;
        this.startTimestamp = startTimestamp;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
}
