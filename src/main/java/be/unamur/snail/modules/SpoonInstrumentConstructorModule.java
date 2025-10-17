package be.unamur.snail.modules;

import be.unamur.snail.core.Context;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.database.DatabasePreparerFactory;
import be.unamur.snail.database.MongoServiceManager;
import be.unamur.snail.database.SimpleDatabasePreparerFactory;
import be.unamur.snail.sentinelbackend.BackendServiceManagerFactory;
import be.unamur.snail.sentinelbackend.SimpleBackendServiceManagerFactoryImpl;
import be.unamur.snail.stages.*;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpoonInstrumentConstructorModule implements Module {
    private static final Logger log = LoggerFactory.getLogger(SpoonInstrumentConstructorModule.class);
    private final List<Stage> stages;

    public SpoonInstrumentConstructorModule() {
        CommandRunner runner = new SimpleCommandRunner();
        MongoServiceManager mongo = new MongoServiceManager(runner, 5, 500);
        BackendServiceManagerFactory backendFactory = new SimpleBackendServiceManagerFactoryImpl();
        DatabasePreparerFactory databaseFactory = new SimpleDatabasePreparerFactory(mongo);
        this.stages = List.of(
                new PrepareBackendStage(runner, backendFactory, databaseFactory),
                //new CloneAndCheckoutRepositoryStage()//,
                new CopyDirectoryStage(),
                new BuildClassPathStage(),
                new InstrumentConstructorsStage(),
                new CopySourceCodeStage(),
                new RunProjectTestsStage()
        );
    }

    @Override
    public void run(Context context) throws Exception {
        log.info("Running SpoonInstrumentConstructorModule");
        for (Stage stage : stages) {
            stage.execute(context);
        }
    }
}
