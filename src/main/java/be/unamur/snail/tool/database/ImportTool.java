package be.unamur.snail.tool.database;

import be.unamur.snail.stages.Stage;

import java.util.List;

public interface ImportTool {
    String getName();
    List<Stage> createPreparationStages();
    List<Stage> createImportStages();
    List<Stage> createCleanupStages();
}
