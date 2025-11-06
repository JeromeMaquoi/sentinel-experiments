package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import be.unamur.snail.tool.energy.serializer.DataSerializer;
import be.unamur.snail.utils.csv.CsvParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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
            log.info("Importing iteration folder: {}", iterationFolder);
            RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
            processFolder(iterationFolder, iteration, log, importConfig);
        }
        log.info("Finished importing results from results root: {}", totalPath);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }

    public <T> void processFolder(Path folder, RunIterationDTO iteration, PipelineLogger log, Config.ImportConfig importConfig) throws IOException {
        Files.walk(folder)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        List<String> folderNames = List.of(path.getParent().toAbsolutePath().toString().split("/"));
                        int n = folderNames.size();
                        if (n < 3) {
                            log.info("Skipping file {} due to insufficient folder hierarchy", path);
                            return;
                        }

                        String scope = String.valueOf(JoularJXMapper.mapScope(folderNames.get(n-3)));
                        String measurementType = String.valueOf(JoularJXMapper.mapMeasurementType(folderNames.get(n-2)));
                        String monitoringType = String.valueOf(JoularJXMapper.mapMonitoringType(folderNames.get(n-1)));
                        log.debug("Scope: {}, MeasurementType: {}, MonitoringType: {}", scope, measurementType, monitoringType);

                        if (!importConfig.getScopes().contains(scope) || !importConfig.getMeasurementTypes().contains(measurementType) || !importConfig.getMonitoringTypes().contains(monitoringType)) {
                            log.debug("Skipping file {} due to import config filters", path);
                            return;
                        }

                        log.info("Importing file: {}", path);

                        CommitSimpleDTO commit = JoularJXMapper.mapCommit();

                        if (Objects.equals(monitoringType, MonitoringType.CALLTREES.toString())) {
                            List<CallTreeMeasurementDTO> dtos = CsvParser.parseCallTreeFile(
                                    path,
                                    JoularJXMapper.mapScope(scope),
                                    JoularJXMapper.mapMeasurementType(measurementType),
                                    JoularJXMapper.mapMonitoringType(monitoringType),
                                    iteration,
                                    commit
                            );
                            log.info("DTOs parsed from file {}: {}", path, dtos.size());
                            String json = serializer.serialize(dtos);
                            httpClient.post("/api/v2/call-tree-measurements-entities", json);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public RunIterationDTO parseIterationFromFolder(Path folder) {
        String folderName = folder.getFileName().toString();
        String[] parts = folderName.split("-");
        int pid = Integer.parseInt(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        return new RunIterationDTO(pid, timestamp);
    }
}
