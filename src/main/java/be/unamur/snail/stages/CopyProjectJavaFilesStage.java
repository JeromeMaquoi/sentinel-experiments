package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.logging.PipelineLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class CopyProjectJavaFilesStage implements Stage {
    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        Config config = Config.getInstance();
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();

        String totalProjectPath = (subProject != null && !subProject.isBlank())
                ? projectName + "/" + subProject
                : projectName;

        String baseResourcePath = "java-files/" + totalProjectPath + "/";
        log.info("Searching for Java files in resources: {}", baseResourcePath);

        ClassLoader cl = getClass().getClassLoader();

        List<String> resourceFiles = listResourceFilesRecursively(cl, baseResourcePath);

        if (resourceFiles.isEmpty()) {
            log.info("No Java files found in {}", baseResourcePath);
            return;
        }

        String repoPath = context.getRepoPath();
        if (repoPath == null || repoPath.isBlank()) {
            throw new MissingContextKeyException("repoPath");
        }
        Path repoRoot = Path.of(repoPath).toAbsolutePath();

        for (String resourceFile : resourceFiles) {
            if (!resourceFile.endsWith(".java")) {
                continue;
            }
            log.debug("Preparing to copy Java file: {}", resourceFile);

            InputStream in = cl.getResourceAsStream(resourceFile);
            if (in == null) {
                log.warn("Resource file not found in classpath: {}", resourceFile);
                continue;
            }

            String relativePath = resourceFile.replaceFirst("^java-files/" + totalProjectPath + "/", "");
            Path targetPath = repoRoot.resolve(relativePath).normalize();
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Copied Java resource: {} to {}", resourceFile, targetPath);
        }

        log.info("Finished copying Java files for project {}", totalProjectPath);
    }

    protected List<String> listResourceFilesRecursively(ClassLoader cl, String basePath) throws IOException {
        List<String> result = new ArrayList<>();

        URL url = cl.getResource(basePath);
        if (url == null) {
            return result;
        }

        if (url.getProtocol().equals("file")) {
            Path folderPath = Path.of(url.getPath());
            if (!Files.exists(folderPath)) return result;

            try (var walk = Files.walk(folderPath)) {
                walk.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String cp = basePath + folderPath.relativize(path).toString().replace("\\", "/");
                            result.add(cp);
                        });
            }
        } else if (url.getProtocol().equals("jar")) {
            String jarPath = url.getPath();
            jarPath = jarPath.substring(5, jarPath.indexOf("!"));

            try (JarFile jar = new JarFile(jarPath)) {
                String prefix = basePath;
                jar.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(entry -> entry.getName().startsWith(prefix))
                        .forEach(entry -> result.add(entry.getName()));
            }
        }
        return result;
    }
}
