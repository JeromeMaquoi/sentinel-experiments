package be.unamur.snail.modules;

import be.unamur.snail.ModuleConfig;
import be.unamur.snail.config.StageConfig;
import be.unamur.snail.core.Module;
import be.unamur.snail.core.StageFactory;

public class SpoonInstrumentConstructorModule extends Module {
    public SpoonInstrumentConstructorModule(ModuleConfig config) {
        if (config.getRepetitions() != null) this.setRepetitions(config.getRepetitions());
        if (config.getPreStage() != null) {
            for (StageConfig stage : config.getPreStage()) preStages.add(StageFactory.create(stage));
        }
        if (config.getStages() != null) {
            for (StageConfig stage : config.getStages()) stages.add(StageFactory.create(stage));
        }
        if (config.getPostStage() != null) {
            for (StageConfig stage : config.getPostStage()) postStages.add(StageFactory.create(stage));
        }
    }
}
