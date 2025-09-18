package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

public class BuildClassPathStage implements Stage {
    private static Logger log = LoggerFactory.getLogger(BuildClassPathStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();

        String projectPath = config.getRepo().getTargetDir() + "_" + config.getRepo().getCommit();
        File projectDir = new File(projectPath);
        if (!projectDir.exists()) throw new IllegalArgumentException("project directory does not exist");
        log.info("Starting build classpath stage for {}", projectPath);

        List<String> classPath;
        if (new File(projectDir, "pom.xml").exists()) {
            classPath = buildMavenClasspath(projectDir);
        } else if (new File(projectDir, "build.gradle").exists()) {
            classPath = buildGradleClasspath(projectDir);
        } else throw new IllegalArgumentException("project directory does not exist");

        log.info("Classpath built with {} entries", classPath.size());
        log.debug("Classpath : {}", classPath);
        context.put("classPath", classPath);
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

        // Create a temporary init script with the task definition
        File initScript = File.createTempFile("sentinel-export-classpath", ".gradle");
        String gradleTaskContent = """
        allprojects {
            tasks.register("exportRuntimeClasspath") {
                doLast {
                    def f = new File(project.projectDir, "classpath.txt")
                    if (configurations.findByName("runtimeClasspath") != null) {
                        f.text = configurations.runtimeClasspath.files.collect { it.absolutePath }.join('\\n')
                    } else {
                        f.text = ""
                    }
                }
            }
        }
        """;
        Files.writeString(initScript.toPath(), gradleTaskContent);
        log.debug("Temporary Gradle init script created at {}", initScript.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(
                "./gradlew", gradleTaskPath, "-I", initScript.getAbsolutePath()
        );
//        ProcessBuilder pb = new ProcessBuilder(
//                "./gradlew", classpathCommand
//        );
        pb.directory(projectRootDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // log all Gradle output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("[gradle] {}", line);
            }
        }

        int exit = process.waitFor();
        if (exit != 0) {
            log.error("Gradle classpath generation failed");
            throw new ModuleException("Gradle classpath generation failed");
        }
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

        log.info("Gradle classpath built");
        log.debug("Classpath: {}", Files.readAllLines(cpFile.toPath()));

        return Files.readAllLines(cpFile.toPath());
    }
}
