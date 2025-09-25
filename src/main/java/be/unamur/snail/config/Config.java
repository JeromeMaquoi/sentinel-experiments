package be.unamur.snail.config;

import be.unamur.snail.exceptions.ConfigNotLoadedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class Config {
    private static Config instance;
    private ProjectConfig project;
    private RepoConfig repo;
    private LogConfig log;
    @JsonProperty("code-constructors-instrumentation-path")
    private String codeConstructorsInstrumentationPath;
    @JsonProperty("command-time-out")
    private int commandTimeout = 120;
    @JsonProperty("execution-plan")
    private ExecutionPlanConfig executionPlan;
    private DatabaseConfig database;

    public static Config getInstance() {
        if (instance == null) {
            throw new ConfigNotLoadedException();
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

    public void setProjectForTests(ProjectConfig project) {
        this.project = project;
    }

    public RepoConfig getRepo() {
        return repo;
    }

    public void setRepoForTests(RepoConfig repo) {
        this.repo = repo;
    }

    public LogConfig getLog() {
        return log;
    }

    public String getCodeConstructorsInstrumentationPath() {
        return codeConstructorsInstrumentationPath;
    }

    public void setCodeConstructorsInstrumentationPathForTests(String codeConstructorsInstrumentationPath) {
        this.codeConstructorsInstrumentationPath = codeConstructorsInstrumentationPath;
    }

    public int getCommandTimeout() {
        return commandTimeout;
    }

    public void setTimeoutForTests(int timeout) {
        this.commandTimeout = timeout;
    }

    public ExecutionPlanConfig getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlanForTests(ExecutionPlanConfig executionPlan) {
        this.executionPlan = executionPlan;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public static class ProjectConfig {
        @JsonProperty("sub-project")
        private String subProject;
        @JsonProperty("show-project-logs")
        private boolean showProjectLogs;
        @JsonProperty("package-prefix")
        private String packagePrefix;

        public String getSubProject() {
            return subProject;
        }

        public void setSubProjectForTests(String subProject) {
            this.subProject = subProject;
        }

        public boolean isShowProjectLogs() {
            return showProjectLogs;
        }

        public String getPackagePrefix() {
            return packagePrefix;
        }
    }




    public static class RepoConfig {
        private String url;
        private String commit;
        @JsonProperty("target-dir")
        private String targetDir;
        private boolean overwrite;

        public String getUrl() {
            return url;
        }

        public String getCommit() {
            return commit;
        }

        public void setCommitForTests(String commit) {
            this.commit = commit;
        }

        public String getTargetDir() {
            return targetDir;
        }

        public void setTargetDirForTests(String targetDir) {
            this.targetDir = targetDir;
        }

        public boolean isOverwrite() {
            return overwrite;
        }

        public void setOverwriteForTests(boolean overwrite) {
            this.overwrite = overwrite;
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


    public static class ExecutionPlanConfig {
        @JsonProperty("test-command")
        private String testCommand;
        @JsonProperty("ignore-failures")
        private boolean ignoreFailures;
        @JsonProperty("ignore-spoon-failures")
        private boolean ignoreSpoonFailures;

        public String getTestCommand() {
            return testCommand;
        }

        public boolean getIgnoreFailures() {
            return ignoreFailures;
        }

        public boolean getIgnoreSpoonFailures() {
            return ignoreSpoonFailures;
        }
    }



    public static class DatabaseConfig {
        private String mode;
        @JsonProperty("backend-timeout-seconds")
        private int backendTimeoutSeconds;
        @JsonProperty("plugins-directory")
        private String pluginsDirectory;
        @JsonProperty("backend-path")
        private String backendPath;
        @JsonProperty("ssh-user")
        private String sshUser;
        @JsonProperty("ssh-host")
        private String sshHost;

        public String getMode() {
            return mode;
        }

        public String getPluginsDirectory() {
            return pluginsDirectory;
        }

        public String getBackendPath() {
            return backendPath;
        }

        public String getSshUser() {
            return sshUser;
        }

        public String getSshHost() {
            return sshHost;
        }

        public int getBackendTimeoutSeconds() {
            return backendTimeoutSeconds;
        }
    }
}
