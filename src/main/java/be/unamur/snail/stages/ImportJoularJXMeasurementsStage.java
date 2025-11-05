package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import be.unamur.snail.utils.csv.CsvParser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ImportJoularJXMeasurementsStage implements Stage {
    private final Path resultsRoot;
    private final MeasurementSender sender;
    private final String commitSha;

    public ImportJoularJXMeasurementsStage(Path resultsRoot, MeasurementSender sender, String commitSha) {
        this.resultsRoot = resultsRoot;
        this.sender = sender;
        this.commitSha = commitSha;
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        if (!Files.exists(resultsRoot) || !Files.isDirectory(resultsRoot)) {
            log.warn("Results root directory does not exist or is not a directory: {}", resultsRoot);
            return;
        }
         List<Path> iterationFolders = Files.list(resultsRoot)
                         .filter(Files::isDirectory)
                        .toList();
        for (Path iterationFolder : iterationFolders) {
            RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
            processFolder(iterationFolder, iteration, log);
        }
        log.info("Finished importing results from results root: {}", resultsRoot);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }

    public <T> void processFolder(Path folder, RunIterationDTO iteration, PipelineLogger log) throws IOException {
        Files.walk(folder)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        List<String> folderNames = List.of(path.getParent().toAbsolutePath().toString().split("/"));
                        int n = folderNames.size();
                        String scope = String.valueOf(JoularJXMapper.mapScope(folderNames.get(n-3)));
                        MeasurementType measurementType = JoularJXMapper.mapMeasurementType(folderNames.get(n-2));
                        String monitoringType = String.valueOf(JoularJXMapper.mapMonitoringType(folderNames.get(n-1)));

                        if (monitoringType.equals(MonitoringType.CALLTREES)) {
                            List<CallTreeMeasurementDTO> dtos = CsvParser.parseCallTreeFile(
                                    path,
                                    JoularJXMapper.mapScope(scope),
                                    measurementType,
                                    JoularJXMapper.mapMonitoringType(monitoringType),
                                    iteration,
                                    null
                            );
                        }
                    } catch (IOException e) {
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
