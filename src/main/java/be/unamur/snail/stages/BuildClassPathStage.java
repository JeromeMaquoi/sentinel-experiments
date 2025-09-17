package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;

import java.io.File;
import java.nio.file.Files;

public class BuildClassPathStage implements Stage {

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String projectPath = config.getProject().getProjectPath();

        File projectDir = new File(projectPath);
        if (!projectDir.exists()) throw new IllegalArgumentException("project directory does not exist");

        String[] classPath;
        if (new File(projectDir, "pom.xml").exists()) {
            classPath = buildMavenClasspath(projectDir);
        } else if (new File(projectDir, "build.gradle").exists()) {
            classPath = buildGradleClasspath(projectDir);
        } else throw new IllegalArgumentException("project directory does not exist");

        System.out.println("Classpath built with " + classPath.length + " entries.");
        context.put("classPath", classPath);
    }

    private String[] buildMavenClasspath(File projectDir) throws Exception {
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

        return Files.readAllLines(cpFile.toPath()).toArray(new String[0]);
    }

    private String[] buildGradleClasspath(File projectDir) throws Exception {
        Config config = Config.getInstance();
        String classpathCommand = config.getProject().getClassPathCommand();
        ProcessBuilder pb = new ProcessBuilder(
                "./gradlew", classpathCommand
        );
        pb.directory(projectDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exit = process.waitFor();
        if (exit != 0) throw new RuntimeException("Gradle classpath build failed");
        File cpFile = new File(projectDir, "classpath.txt");
        if (!cpFile.exists()) throw new RuntimeException("Classpath file not found: " + cpFile.getAbsolutePath());

        return Files.readAllLines(cpFile.toPath()).toArray(new String[0]);
    }
}
