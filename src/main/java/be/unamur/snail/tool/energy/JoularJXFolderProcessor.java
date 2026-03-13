package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class JoularJXFolderProcessor implements FolderProcessor {
    private final JoularJXFileProcessor fileProcessor;
    private final PipelineLogger log;

    public JoularJXFolderProcessor(JoularJXFileProcessor fileProcessor, PipelineLogger log) {
        this.fileProcessor = fileProcessor;
        this.log = log;
    }

    @Override
    public void processFolder(Path folder, RunIterationDTO iteration, Context context) throws IOException {
        log.debug("Processing JoularJX iteration folder: {}", folder);
        try (Stream<Path> files = Files.walk(folder)) {
            List<Path> csvFiles = files
                    .filter(Files::isRegularFile)
                    .filter(this::isCsvFile)
                    .toList();
            log.debug("Found {} csv files in {}",  csvFiles.size(), folder);
            for (Path csvFile : csvFiles) {
                processCsvFile(csvFile, iteration, context);
            }
        }
        log.debug("Finished processing folder {}", folder);
    }

    protected void processCsvFile(Path csvFile, RunIterationDTO iteration, Context context) {
        log.debug("Processing csv file: {}", csvFile);
        try {
            fileProcessor.processFile(csvFile, iteration, context);
        } catch (Exception e) {
            log.error("Error processing file {}: {}", csvFile, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected boolean isCsvFile(Path file) {
        return file.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(".csv");
    }
}
