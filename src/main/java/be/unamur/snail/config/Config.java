package be.unamur.snail.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class Config {
    private static Config instance;
    @JsonProperty("output-dir")
    private String outputDir;
    private ProjectConfig project;

    private Config() {}

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Config not loaded yet");
        }
        return instance;
    }

    public static void load(String yamlPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        instance = mapper.readValue(new File(yamlPath), Config.class);
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public ProjectConfig getProject() {
        return project;
    }

    public void setProject(ProjectConfig project) {
        this.project = project;
    }

    public static class ProjectConfig {
        @JsonProperty("classpath-command")
        private String classPathCommand;

        @JsonProperty("project-path")
        private String projectPath;

        public String getClassPathCommand() {
            return classPathCommand;
        }

        public void setClassPathCommand(String classPathCommand) {
            this.classPathCommand = classPathCommand;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }
    }
}
