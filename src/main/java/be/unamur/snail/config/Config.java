package be.unamur.snail.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class Config {
    private static Config instance;
    private ProjectConfig project;
    private RepoConfig repo;
    private LogConfig log;

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Config not loaded yet");
        }
        return instance;
    }

    public static void setInstanceForTests(Config config) {
        instance = config;
    }

    public static void reset() {
        instance = null;
    }

    public static void load(String yamlPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        instance = mapper.readValue(new File(yamlPath), Config.class);
    }

    public ProjectConfig getProject() {
        return project;
    }

    public RepoConfig getRepo() {
        return repo;
    }

    public LogConfig getLog() {
        return log;
    }

    public static class ProjectConfig {
        @JsonProperty("sub-project")
        private String subProject;

        public String getSubProject() {
            return subProject;
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

    public static class LogConfig {
        private String level;

        public String getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return "LogConfig{" +
                    "level='" + level + '\'' +
                    '}';
        }
    }
}
