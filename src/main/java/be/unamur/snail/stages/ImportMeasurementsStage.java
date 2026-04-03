package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.services.MeasurementsImportService;
import java.nio.file.Path;

public class ImportMeasurementsStage implements Stage {
    private final Path resultsRoot;
    private final MeasurementsImportService service;

    public ImportMeasurementsStage(Path resultsRoot, MeasurementsImportService service) {
        this.resultsRoot = resultsRoot;
        this.service = service;
    }

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String targetDir = config.getRepo().getTargetDir();
        if (targetDir == null || targetDir.isBlank()) {
            throw new MissingConfigKeyException("repo.target-dir");
        }
        String repoDir = targetDir + config.getProject().getName() + "_measurements_" + config.getRepo().getCommit();
        service.importMeasurements(resultsRoot, repoDir, context);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }
}
