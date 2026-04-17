package be.unamur.snail.modules;

import be.unamur.snail.core.Config;
import be.unamur.snail.stages.Stage;
import be.unamur.snail.stages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SpoonInstrumentConstructorModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(SpoonInstrumentConstructorModule.class);
    private final List<Stage> stages;

    SpoonInstrumentConstructorModule(List<Stage> stages) {
        this.stages = stages;
    }

    public SpoonInstrumentConstructorModule() {
        Config config = Config.getInstance();
        String repoDir = buildRepoDir(config);

        this.stages = Stream.of(
                new StopBackendStage(),
                new PrepareBackendStage(),
                new CloneAndCheckoutRepositoryStage(repoDir),
                createCopyBuildFileStageForClasspath(),
                new BuildClassPathStage(),
                createCopyBuildFileStage(),
                new InstrumentConstructorsStage(),
                new CopySourceCodeStage(),
                new CopyProjectJavaFilesStage(),
                new RunInstrumentedProjectTestsStage()
        ).filter(Objects::nonNull).toList();
    }

    @Override
    protected List<Stage> getStages() {
        return stages;
    }

    public static String buildRepoDir(Config config) {
        return config.getProject().getName() + "_instrumentation_" + config.getRepo().getCommit();
    }

    protected CopyFileStage createCopyBuildFileStageForClasspath() {
        Config config = Config.getInstance();
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();

        String totalProjectPath = createTotalProjectPath(projectName, subProject);
        String buildFileName = detectBuildFileNameOrNullForClasspath(totalProjectPath);
        if (buildFileName == null) {
            log.info("No class path build file found for project {}, skipping CopyFileStage creation.", totalProjectPath);
            return null;
        }

        Path sourceFile = buildResourcePath(totalProjectPath + "/classpath", buildFileName);
        Path relativeTargetPath = Path.of(subProject).resolve(buildFileName);

        log.debug("Configured CopyFileStage for classpath {}: {} -> {}", projectName, sourceFile, relativeTargetPath);
        return new CopyFileStage(sourceFile, relativeTargetPath);
    }

    public String detectBuildFileNameOrNullForClasspath(String totalProjectPath) {
        String basePath = String.format("build-files/%s/classpath/", totalProjectPath);
        log.debug("Base path for build file detection: {}", basePath);
        URL gradleGroovyURL = getClass().getClassLoader().getResource(basePath + "build.gradle");
        URL gradleKotlinURL = getClass().getClassLoader().getResource(basePath + "build.gradle.kts");
        URL mavenURL = getClass().getClassLoader().getResource(basePath + "pom.xml");

        if (gradleGroovyURL != null) {
            return "build.gradle";
        } else if (gradleKotlinURL != null) {
            return "build.gradle.kts";
        } else if (mavenURL != null) {
            return "pom.xml";
        } else {
            return null;
        }
    }

    protected CopyFileStage createCopyBuildFileStage() {
        Config config = Config.getInstance();
        String projectName = config.getProject().getName();
        String subProject = config.getProject().getSubProject();

        String totalProjectNamePath = createTotalProjectPath(projectName, subProject);
        String buildFileName = detectBuildFileNameOrNull(totalProjectNamePath);
        if (buildFileName == null) {
            log.info("No instrumentation build file found for project {}, skipping CopyFileStage creation.", totalProjectNamePath);
            return null;
        }

        Path sourceFile = buildResourcePath(totalProjectNamePath + "/instrumentation", buildFileName);
        Path relativeTargetPath = Path.of(subProject).resolve(buildFileName);

        log.info("Configured CopyFileStage for instrumentation {}: {} -> {}", buildFileName, sourceFile, relativeTargetPath);
        return new CopyFileStage(sourceFile, relativeTargetPath);
    }

    public String createTotalProjectPath(String projectName, String subProject) {        return (subProject != null && !subProject.isBlank()) ? projectName + "/" + subProject : projectName;
    }

    public String detectBuildFileNameOrNull(String totalProjectPath) {
        String basePath = String.format("build-files/%s/instrumentation/", totalProjectPath);

        URL gradleURL = getClass().getClassLoader().getResource(basePath + "build.gradle");
        URL mavenURL = getClass().getClassLoader().getResource(basePath + "pom.xml");

        if (gradleURL != null) {
            return "build.gradle";
        } else if (mavenURL != null) {
            return "pom.xml";
        } else {
            return null;
        }
    }

    public Path buildResourcePath(String totalProjectPath, String fileName) {
        return Path.of("resources", "build-files")
                .resolve(totalProjectPath)
                .resolve(fileName);
    }
}
