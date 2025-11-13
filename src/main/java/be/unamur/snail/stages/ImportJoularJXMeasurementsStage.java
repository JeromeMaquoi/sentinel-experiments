package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import be.unamur.snail.tool.energy.serializer.DataSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportJoularJXMeasurementsStage implements Stage {
    private final Path resultsRoot;
    private final SimpleHttpClient httpClient;
    private final DataSerializer serializer;

    public ImportJoularJXMeasurementsStage(Path resultsRoot) {
        this(resultsRoot, new SimpleHttpClient(), new DataSerializer());
    }

    public ImportJoularJXMeasurementsStage(Path resultsRoot, SimpleHttpClient httpClient, DataSerializer serializer) {
        this.resultsRoot = resultsRoot;
        this.httpClient = httpClient;
        this.serializer = serializer;
    }

    @Override
    public void execute(Context context) throws Exception {
        if (context.getRepoPath() == null || context.getRepoPath().isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }
        Config config = Config.getInstance();
        Config.ImportConfig importConfig = config.getExecutionPlan().getEnergyMeasurements().getImportConfig();

        PipelineLogger log = context.getLogger();

        Path totalPath = Path.of(context.getRepoPath()).resolve(resultsRoot).normalize();
        log.debug("Starting importing results from results root: {}", totalPath);

        if (!Files.exists(totalPath) || !Files.isDirectory(totalPath)) {
            throw new SourceDirectoryNotFoundException(totalPath);
        }
         List<Path> iterationFolders = Files.list(totalPath)
                         .filter(Files::isDirectory)
                        .toList();
        for (Path iterationFolder : iterationFolders) {
            RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
            processFolder(iterationFolder, iteration, log, importConfig, context);
            log.info("Iteration folder '{}' imported", iterationFolder);
        }
        log.info("Finished importing results from results root: {}", totalPath);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }

    public void processFolder(Path folder, RunIterationDTO iteration, PipelineLogger log, Config.ImportConfig importConfig, Context context) throws IOException {
        JoularJXFileProcessor fileProcessor = new JoularJXFileProcessor(serializer, httpClient, importConfig, log);
        new JoularJXFolderProcessor(fileProcessor, log).processFolder(folder, iteration, context);
    }

    public RunIterationDTO parseIterationFromFolder(Path folder) {
        String folderName = folder.getFileName().toString();
        String[] parts = folderName.split("-");
        int pid = Integer.parseInt(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        return new RunIterationDTO(pid, timestamp);
    }
}
