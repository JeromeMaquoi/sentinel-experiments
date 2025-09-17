package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CloneAndCheckoutRepositoryStage implements Stage {
    @Override
    public void execute(Context context) throws Exception {
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

        System.out.println("Cloning " + repoUrl + " to " + targetDirStr);

        try (Git git = Git.cloneRepository().setURI(repoUrl).setDirectory(targetDir).call()) {
            git.checkout().setName(commit).call();
            System.out.println("Checked out " + commit);
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
