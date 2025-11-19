package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

/**
 * Stage responsible for cloning the analyzed project and checkout to the
 * desired commit
 */
public class CloneAndCheckoutRepositoryStage implements Stage {
    @Override
    public void execute(Context context) {
        PipelineLogger log = context.getLogger();

        Config config = Config.getInstance();
        String repoUrl = config.getRepo().getUrl();
        String commit = config.getRepo().getCommit();
        String targetDirStr = config.getRepo().getTargetDir();

        if (repoUrl == null || commit == null || targetDirStr == null) {
            throw new IllegalArgumentException("Config must include repo.url, repo.commit, and repo.target-dir");
        }

        File targetDir = new File(targetDirStr);
        log.debug("targetDir is {}", targetDir.getAbsolutePath());
        if (targetDir.exists() && !config.getRepo().isOverwriteClone()) {
            // TODO: better handle the overwrite management
            log.info("Project already cloned. Skipping this step");
            context.setCommit(commit);
            context.setRepoPath(targetDirStr);
            return;
        } else if (targetDir.exists() && config.getRepo().isOverwriteClone()) {
            deleteDirectory(targetDir);
        }

        log.info("Cloning {} to {}", repoUrl, targetDirStr);
        context.setRepoPath(targetDirStr);

        try (Git git = Git.cloneRepository().setURI(repoUrl).setDirectory(targetDir).call()) {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commit).call();
            context.setCommit(commit);
            log.info("Checked out commit {}", commit);
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to clone or checkout " + commit, e);
        }
    }
}
