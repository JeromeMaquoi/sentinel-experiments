package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.io.IOException;
import java.nio.file.Path;

public interface FolderProcessor {
    void processFolder(Path path, RunIterationDTO iteration, Context context) throws IOException;
}
