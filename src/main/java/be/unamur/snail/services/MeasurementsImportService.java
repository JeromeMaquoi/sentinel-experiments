package be.unamur.snail.services;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.files.DirectoryService;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.FolderProcessor;
import be.unamur.snail.tool.energy.FolderProcessorFactory;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.nio.file.Path;
import java.util.List;

/**
 * Service for importing energy measurements from CSV files.
 * Handles folder traversal, iteration parsing, and report generation.
 * Ensures no data is lost by tracking all processed files and generating detailed reports.
 */
public class MeasurementsImportService {
    private final DirectoryService directoryService;
    private final FolderProcessorFactory processorFactory;

    public MeasurementsImportService(DirectoryService directoryService, FolderProcessorFactory processorFactory) {
        this.directoryService = directoryService;
        this.processorFactory = processorFactory;
    }

    /**
     * Import measurements from a directory structure.
     * 
     * @param resultsRoot The root directory name within targetDir
     * @param targetDir The target directory path
     * @param context The pipeline context
     * @return ImportReport containing detailed statistics about the import
     * @throws Exception if the source directory is not found
     */
    public ImportReport importMeasurements(Path resultsRoot, String targetDir, Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        Path totalPath = Path.of(targetDir).resolve(resultsRoot).normalize();

        if (!directoryService.exists(totalPath) || !directoryService.isDirectory(totalPath)) {
            throw new SourceDirectoryNotFoundException(totalPath);
        }

        log.info("Starting measurements import from: {}", totalPath);
        FolderProcessor processor = processorFactory.create(context);

        List<Path> iterationFolders = directoryService.listDirectories(totalPath);
        log.info("Found {} iteration folders to process", iterationFolders.size());

        ImportReport report = new ImportReport(totalPath.toString());

        for (Path iterationFolder : iterationFolders) {
            log.info("Processing iteration folder: {}", iterationFolder);
            try {
                RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
                processor.processFolder(iterationFolder, iteration, context);
            } catch (Exception e) {
                log.error("Error processing iteration folder {}: {}", iterationFolder, e.getMessage(), e);
                report.addBackendError(String.format("Failed to process folder %s: %s", iterationFolder, e.getMessage()));
            }
        }

        report.markComplete();
        log.info("Measurement import completed. Summary: {}", report);
        
        return report;
    }

    /**
     * Parse iteration information from folder name.
     * Expected format: {pid}-{timestamp}
     *
     * @param folder The iteration folder
     * @return RunIterationDTO with parsed pid and timestamp
     * @throws IllegalArgumentException if folder name doesn't match expected format
     */
    protected RunIterationDTO parseIterationFromFolder(Path folder) {
        String[] parts = folder.getFileName().toString().split("-");
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid folder name format: %s. Expected format: {pid}-{timestamp}",
                            folder.getFileName())
            );
        }
        
        try {
            int pid = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            return new RunIterationDTO(pid, timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse iteration folder name %s: %s",
                            folder.getFileName(), e.getMessage()), e
            );
        }
    }
}
