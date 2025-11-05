package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.CsvLineMapper;
import be.unamur.snail.tool.energy.JoularJXMapper;
import be.unamur.snail.tool.energy.MeasurementSender;
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
         Files.list(resultsRoot)
                 .filter(Files::isDirectory)
                 .forEach(pidFolder -> {
                     try {
                         RunIterationDTO iteration = parseIterationFromFolder(pidFolder);

                         Path callTreePath = pidFolder.resolve("app/runtime/calltrees");
                         Path methodsPath = pidFolder.resolve("app/runtime/methods");

                         processFolder(callTreePath, iteration, log, JoularJXMapper::mapCallTreelLine, "/api/v2/call-tree-measurements-entities");

                         Path totalCalltreePath = pidFolder.resolve("app/total/calltrees");
                         processFolder(totalCalltreePath, iteration, log, JoularJXMapper::mapCallTreelLine, "/api/v2/call-tree-measurements-entities");
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        log.info("Finished importing results from results root: {}", resultsRoot);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }

    public <T> void processFolder(Path folder, RunIterationDTO iteration, PipelineLogger log, CsvLineMapper<T> mapper, String endpoint) throws IOException {
        if (!Files.exists(folder)) return;
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(f -> f.toString().endsWith(".csv"))
                    .forEach(file -> {
                        try {
                            List<T> dtos = CsvParser.parseCsvFile(file, iteration, commitSha, mapper);
                            sender.sendMeasurement(dtos, endpoint);
                        } catch (IOException e) {
                            log.error("Failed to parse CSV file: {}", file, e);
                        }
                    });
        }
    }

    public RunIterationDTO parseIterationFromFolder(Path folder) {
        String folderName = folder.getFileName().toString();
        String[] parts = folderName.split("-");
        int pid = Integer.parseInt(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        return new RunIterationDTO(pid, timestamp);
    }
}
