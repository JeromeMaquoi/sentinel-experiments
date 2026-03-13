package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JoularJXFolderProcessor {
    private final JoularJXFileProcessor fileProcessor;
    private final PipelineLogger log;

    public JoularJXFolderProcessor(JoularJXFileProcessor fileProcessor, PipelineLogger log) {
        this.fileProcessor = fileProcessor;
        this.log = log;
    }

    public void processFolder(Path folder, RunIterationDTO iteration, Context context) throws IOException {
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> fileProcessor.process(path, iteration, context));
        }
    }
}
