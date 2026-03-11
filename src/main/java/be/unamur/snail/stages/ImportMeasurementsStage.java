package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.SourceDirectoryNotFoundException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.*;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportMeasurementsStage implements Stage {
    private final Path resultsRoot;
    private final FolderProcessorFactory processorFactory;

    public ImportMeasurementsStage(Path resultsRoot, FolderProcessorFactory processorFactory) {
        this.resultsRoot = resultsRoot;
        this.processorFactory = processorFactory;
    }

    @Override
    public void execute(Context context) throws Exception {
        if (context.getRepoPath() == null || context.getRepoPath().isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }

        PipelineLogger log = context.getLogger();

        Path totalPath = Path.of(context.getRepoPath()).resolve(resultsRoot).normalize();
        log.debug("Starting importing results from results root: {}", totalPath);

        if (!Files.exists(totalPath) || !Files.isDirectory(totalPath)) {
            throw new SourceDirectoryNotFoundException(totalPath);
        }

        FolderProcessor processor = processorFactory.create(context);

        List<Path> iterationFolders = Files.list(totalPath)
                         .filter(Files::isDirectory)
                        .toList();

        for (Path iterationFolder : iterationFolders) {
            log.info("Importing iteration folder: {}", iterationFolder);
            RunIterationDTO iteration = parseIterationFromFolder(iterationFolder);
            processor.processFolder(iterationFolder, iteration, context);
        }
        log.info("Finished importing results from results root: {}", totalPath);
    }

    @Override
    public String getName() {
        return Stage.super.getName();
    }

    public RunIterationDTO parseIterationFromFolder(Path folder) {
        String folderName = folder.getFileName().toString();
        String[] parts = folderName.split("-");
        int pid = Integer.parseInt(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        return new RunIterationDTO(pid, timestamp);
    }
}
