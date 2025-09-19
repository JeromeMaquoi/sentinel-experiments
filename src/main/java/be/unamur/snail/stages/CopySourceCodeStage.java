package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CopySourceCodeStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(CopySourceCodeStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String fromDir = config.getCodeConstructorsInstrumentationPath();
        Path from = Paths.get(fromDir);

        String targetDir = config.getRepo().getTargetDir();
        String commit = config.getRepo().getCommit();
        String subProject = config.getProject().getSubProject();

        if (targetDir == null || targetDir.isBlank()) {
            throw new ModuleException("Missing required context key: target-dir");
        }
        if (commit == null || commit.isBlank()) {
            throw new ModuleException("Missing required context key: commit");
        }

        String targetProjectName = targetDir + "_" + commit + subProject + "src/main/java/be/unamur/snail/";
        Path target = Paths.get(targetProjectName).toAbsolutePath();
        log.info("Copying source code from {} to {}", from, target);
    }
}
