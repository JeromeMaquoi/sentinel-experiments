package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.ModuleException;
import be.unamur.snail.exceptions.TargetDirectoryNotFoundException;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.gradle.DefaultGradleService;
import be.unamur.snail.utils.gradle.GradleService;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Stage responsible for the creation of the class path of the analyzed
 * project, so that it can be used by Spoon in other stages
 */
public class BuildClassPathStage implements Stage {
    private final GradleService gradleService;
    private final InitScriptGenerator initScriptGenerator;
    private PipelineLogger log;

    public BuildClassPathStage() {
        this.gradleService = new DefaultGradleService();
        this.initScriptGenerator = new InitScriptGenerator();
    }

    public BuildClassPathStage(GradleService gradleService, InitScriptGenerator initScriptGenerator) {
        this.gradleService = gradleService;
        this.initScriptGenerator = initScriptGenerator;
    }

    @Override
    public void execute(Context context) throws Exception {
        log = context.getLogger();

        Config config = Config.getInstance();

        String projectPath = config.getRepo().getTargetDir() + "_" + config.getRepo().getCommit();
        File projectDir = new File(projectPath);
        if (!projectDir.exists()) throw new TargetDirectoryNotFoundException();
        log.info("Starting build classpath stage for {}", projectPath);

        List<String> classPath;
        if (new File(projectDir, "pom.xml").exists()) {
            classPath = buildMavenClasspath(projectDir);
        } else if (new File(projectDir, "build.gradle").exists()) {
            classPath = buildGradleClasspath(projectDir);
        } else throw new IllegalArgumentException("project directory does not exist");

        log.info("Classpath built with {} entries", classPath.size());
        log.debug("Classpath : {}", classPath);
        context.setClassPath(classPath);
    }

    private List<String> buildMavenClasspath(File projectDir) throws Exception {
        // Runs: mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
        ProcessBuilder pb = new ProcessBuilder(
                "mvn", "dependency:build-classpath", "-Dmdep.outputFile=classpath.txt"
        );
        pb.directory(projectDir);
        pb.inheritIO();
        Process process = pb.start();
        int exit = process.waitFor();
        if (exit != 0) throw new RuntimeException("Maven classpath build failed");

        File cpFile = new File(projectDir, "cp.txt");
        if (!cpFile.exists()) throw new RuntimeException("Classpath file not found: " + cpFile.getAbsolutePath());

        log.info("Maven classpath built");
        return Files.readAllLines(cpFile.toPath());
    }


    private List<String> buildGradleClasspath(File projectRootDir) throws Exception {
        Config config = Config.getInstance();

        // Determine task scope (root or sub-project)
        String subProject = config.getProject().getSubProject();
        String gradleTaskPath = "exportRuntimeClasspath";
        if (subProject != null && !subProject.isBlank()) {
            gradleTaskPath = subProject.replaceAll("^/|/$", "").replace("/", ":") + ":exportRuntimeClasspath";
        }

        File initScript = initScriptGenerator.generateClasspathInitScript();
        log.debug("Temporary Gradle init script created at {}", initScript.getAbsolutePath());

        gradleService.runTask(projectRootDir, gradleTaskPath, initScript);

        // classpath.txt is written inside the subproject (if given), else root project
        File cpFile;
        if (subProject != null && !subProject.isBlank()) {
            cpFile = new File(new File(projectRootDir, subProject), "classpath.txt");
        } else {
            cpFile = new File(projectRootDir, "classpath.txt");
        }
        if (!cpFile.exists()) {
            log.error("Classpath file not found: {}", cpFile.getAbsolutePath());
            throw new ModuleException("Classpath file not found: " + cpFile.getAbsolutePath());
        }

        log.debug("Gradle classpath built");
        log.debug("Classpath: {}", Files.readAllLines(cpFile.toPath()));

        return Files.readAllLines(cpFile.toPath());
    }
}
