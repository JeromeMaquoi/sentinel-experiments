package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.BuildFileNotFoundException;
import be.unamur.snail.stages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class JoularJXTool implements EnergyMeasurementTool {
    private static final Logger log = LoggerFactory.getLogger(JoularJXTool.class);
    private final Config config;

    public JoularJXTool() {
        this(Config.getInstance());
    }

    public JoularJXTool(Config config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return "JoularJX";
    }

    @Override
    public List<Stage> createSetupStages() {
        return List.of(
                new RetrieveToolReleaseStage(),
                //new UpdateBuildConfigurationStage()
                createCopyBuildFileStage(),
                new UpdateBuildFileStage(),
                new SetupJdkStage()
        );
    }

    @Override
    public List<Stage> createMeasurementStages() {
        return List.of(
                new SetDirectoryStage()
        );
    }

    @Override
    public List<Stage> createPostProcessingStages() {
        return List.of();
    }

    protected CopyFileStage createCopyBuildFileStage() {
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();
        String buildFileName = detectBuildFileName(projectName, subProject);

        Path sourceFile = Path.of("resources", "build-files")
                .resolve(projectName)
                .resolve(subProject)
                .resolve(buildFileName);

        Path relativeTargetPath = Path.of(subProject).resolve(buildFileName);

        log.info("Configured CopyFileStage for {}: {} -> {}", projectName, sourceFile, relativeTargetPath);
        return new CopyFileStage(sourceFile, relativeTargetPath);
    }

    public String detectBuildFileName(String projectName, String subProject) {
        String basePath = String.format("build-files/%s/%s/", projectName, subProject);
        URL gradleURL = getClass().getClassLoader().getResource(basePath + "build.gradle");
        URL mavenURL = getClass().getClassLoader().getResource(basePath + "pom.xml");

        if (gradleURL != null) {
            return "build.gradle";
        } else if (mavenURL != null) {
            return "pom.xml";
        } else {
            throw new BuildFileNotFoundException(projectName, subProject);
        }
    }
}
