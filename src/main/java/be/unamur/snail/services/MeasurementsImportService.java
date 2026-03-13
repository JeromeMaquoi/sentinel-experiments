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

public class MeasurementsImportService {
    private final DirectoryService directoryService;
    private final FolderProcessorFactory processorFactory;

    public MeasurementsImportService(DirectoryService directoryService, FolderProcessorFactory processorFactory) {
        this.directoryService = directoryService;
        this.processorFactory = processorFactory;
    }

    public void importMeasurements(Path resultsRoot, String targetDir, Context context) throws Exception {
        PipelineLogger log = context.getLogger();
        Path totalPath = Path.of(targetDir).resolve(resultsRoot).normalize();

        if (!directoryService.exists(totalPath) || !directoryService.isDirectory(totalPath)) {
            throw new SourceDirectoryNotFoundException(totalPath);
        }

        FolderProcessor processor = processorFactory.create(context);

        List<Path> iterationFolders = directoryService.listDirectories(totalPath);

        for (Path iterationFolder : iterationFolders) {
            log.info("Importing iteration folder: {}", iterationFolder);
            RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
            processor.processFolder(iterationFolder, iteration, context);
        }
    }

    protected RunIterationDTO parseIterationFromFolder(Path folder) {
        String[] parts = folder.getFileName().toString().split("-");
        int pid = Integer.parseInt(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        return new RunIterationDTO(pid, timestamp);
    }
}
