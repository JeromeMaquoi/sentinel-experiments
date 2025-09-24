package be.unamur.snail.modules;

import be.unamur.snail.core.Context;
import be.unamur.snail.core.Module;
import be.unamur.snail.core.Stage;
import be.unamur.snail.stages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SpoonInstrumentConstructorModule implements Module {
    private static final Logger log = LoggerFactory.getLogger(SpoonInstrumentConstructorModule.class);
    private final List<Stage> stages = Arrays.asList(
        //new CloneAndCheckoutRepositoryStage()//,
        //new CopyDirectoryStage(),
        //new BuildClassPathStage(),
        //new InstrumentConstructorsStage(),
        new CopySourceCodeStage(),
        new RunProjectTestsStage()
    );
    @Override
    public void run(Context context) throws Exception {
        log.info("Running SpoonInstrumentConstructorModule");
        for (Stage stage : stages) {
            stage.execute(context);
        }
    }
}
