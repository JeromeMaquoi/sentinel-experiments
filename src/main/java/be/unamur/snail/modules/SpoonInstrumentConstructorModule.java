package be.unamur.snail.modules;

import be.unamur.snail.core.Context;
import be.unamur.snail.core.Module;
import be.unamur.snail.core.Stage;
import be.unamur.snail.stages.BuildClassPathStage;
import be.unamur.snail.stages.InstrumentConstructorsStage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SpoonInstrumentConstructorModule implements Module {
    private final List<Stage> stages = Arrays.asList(
        new BuildClassPathStage(),
        new InstrumentConstructorsStage()
    );
    @Override
    public void run(Context context) throws Exception {
        for (Stage stage : stages) {
            stage.execute(context);
        }
    }
}
