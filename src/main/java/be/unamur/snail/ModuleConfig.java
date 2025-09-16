package be.unamur.snail;

import be.unamur.snail.config.StageConfig;

import java.util.List;

public class ModuleConfig {
    private String type;
    private Integer repetitions;
    private List<StageConfig> preStage;
    private List<StageConfig> stages;
    private List<StageConfig> postStage;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public List<StageConfig> getPreStage() {
        return preStage;
    }

    public void setPreStage(List<StageConfig> preStage) {
        this.preStage = preStage;
    }

    public List<StageConfig> getStages() {
        return stages;
    }

    public void setStages(List<StageConfig> stages) {
        this.stages = stages;
    }

    public List<StageConfig> getPostStage() {
        return postStage;
    }

    public void setPostStage(List<StageConfig> postStage) {
        this.postStage = postStage;
    }
}
