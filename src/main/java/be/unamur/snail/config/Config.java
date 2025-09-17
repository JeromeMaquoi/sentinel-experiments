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
    private RepoConfig repo;

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

    public ProjectConfig getProject() {
        return project;
    }

    public RepoConfig getRepo() {
        return repo;
    }

    @Override
    public String toString() {
        return "Config{" +
                "outputDir='" + outputDir + '\'' +
                ", project=" + project +
                ", repo=" + repo +
                '}';
    }

    public static class ProjectConfig {
        @JsonProperty("classpath-command")
        private String classPathCommand;

        public String getClassPathCommand() {
            return classPathCommand;
        }

        @Override
        public String toString() {
            return "ProjectConfig{" +
                    "classPathCommand='" + classPathCommand + '\'' +
                    '}';
        }
    }

    public static class RepoConfig {
        private String url;
        private String commit;
        @JsonProperty("target-dir")
        private String targetDir;

        public String getUrl() {
            return url;
        }

        public String getCommit() {
            return commit;
        }

        public String getTargetDir() {
            return targetDir;
        }

        @Override
        public String toString() {
            return "RepoConfig{" +
                    "url='" + url + '\'' +
                    ", commit='" + commit + '\'' +
                    ", targetDir='" + targetDir + '\'' +
                    '}';
        }
    }
}
