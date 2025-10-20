package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CloneAndCheckoutRepositoryStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(CloneAndCheckoutRepositoryStage.class);

    @Override
    public void execute(Context context) {
        Config config = Config.getInstance();
        String repoUrl = config.getRepo().getUrl();
        String commit = config.getRepo().getCommit();
        String targetDirStr = config.getRepo().getTargetDir();

        if (repoUrl == null || commit == null || targetDirStr == null) {
            throw new IllegalArgumentException("Config must include repo.url, repo.commit, and repo.target-dir");
        }

        File targetDir = new File(targetDirStr);
        if (targetDir.exists()) {
            deleteDirectory(targetDir);
        }

        log.info("Cloning {} to {}", repoUrl, targetDirStr);
        context.setRepoPath(targetDirStr);

        try (Git git = Git.cloneRepository().setURI(repoUrl).setDirectory(targetDir).call()) {
            git.checkout().setName(commit).call();
            context.setCommit(commit);
            log.info("Checked out commit {}", commit);
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to clone or checkout " + commit, e);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
