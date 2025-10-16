package be.unamur.snail.modules;

import be.unamur.snail.core.Context;
import be.unamur.snail.core.Module;
import be.unamur.snail.core.Stage;
import be.unamur.snail.stages.CloneAndCheckoutRepositoryStage;
import be.unamur.snail.stages.PackageProjectStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CkModule implements Module {
    private static final Logger log = LoggerFactory.getLogger(CkModule.class);
    private final List<Stage> stages;

    public CkModule() {
        this.stages = List.of(
                new CloneAndCheckoutRepositoryStage(),
                new PackageProjectStage()
        );
    }

    @Override
    public void run(Context context) throws Exception {
        log.info("Running CK module");
        for (Stage stage : stages) {
            stage.execute(context);
        }
    }
}
