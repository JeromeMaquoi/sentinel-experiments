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
                createCopyConfigFileStage(),
                new SetupJdkStage()
        );
    }

    @Override
    public List<Stage> createMeasurementStages() {
        return List.of(
                new SetDirectoryStage(),
                new RunProjectTestsStage()
        );
    }

    @Override
    public List<Stage> createPostProcessingStages() {
        return List.of();
    }

    /**
     * This method creates a new CopyFileStage for copying the appropriate build file
     * (either build.gradle or pom.xml) based on the project configuration.
     * @return A CopyFileStage configured to copy the correct build file.
     */
    protected CopyFileStage createCopyBuildFileStage() {
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();

        String totalProjectPath = createTotalProjectPath(projectName, subProject);
        String buildFileName = detectBuildFileName(totalProjectPath);

        Path sourceFile = buildResourcePath(totalProjectPath, buildFileName);
        Path relativeTargetPath = Path.of(subProject).resolve(buildFileName);

        log.info("Configured CopyFileStage for {}: {} -> {}", projectName, sourceFile, relativeTargetPath);
        return new CopyFileStage(sourceFile, relativeTargetPath);
    }

    /**
     * Create a CopyFileStage to copy the config.properties file into the analyzed
     * project directory
     * @return A CopyFileStage for copying config.properties
     */
    protected CopyFileStage createCopyConfigFileStage() {
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();
        String totalProjectPath = createTotalProjectPath(projectName, subProject);

        Path sourceFile = buildResourcePath(totalProjectPath, "config.properties");
        Path relativeTargePath = Path.of(subProject != null && !subProject.isBlank() ? subProject : "")
                .resolve("config.properties");

        log.info("Configured CopyFileStage for config.properties: {} -> {}", sourceFile, relativeTargePath);
        return new CopyFileStage(sourceFile, relativeTargePath);
    }

    public Path buildResourcePath(String totalProjectPath, String fileName) {
        return Path.of("resources", "build-files")
                .resolve(totalProjectPath)
                .resolve(fileName);
    }

    public String createTotalProjectPath(String projectName, String subProject) {
        return (subProject != null && !subProject.isBlank()) ? projectName + "/" + subProject : projectName;
    }

    public String detectBuildFileName(String totalProjectPath) {
        String basePath = String.format("build-files/%s/", totalProjectPath);
        log.debug("Base path for build file detection: {}", basePath);
        URL gradleURL = getClass().getClassLoader().getResource(basePath + "build.gradle");
        URL mavenURL = getClass().getClassLoader().getResource(basePath + "pom.xml");

        if (gradleURL != null) {
            return "build.gradle";
        } else if (mavenURL != null) {
            return "pom.xml";
        } else {
            throw new BuildFileNotFoundException(totalProjectPath);
        }
    }
}
