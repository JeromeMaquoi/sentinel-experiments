package be.unamur.snail.core;

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
    private BackendConfig backend;

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

    public BackendConfig getBackend() {
        return backend;
    }

    public void setBackendForTests(BackendConfig backend) {
        this.backend = backend;
    }



    public static class ProjectConfig {
        private String name;
        @JsonProperty("sub-project")
        private String subProject;
        @JsonProperty("show-project-logs")
        private boolean showProjectLogs;
        @JsonProperty("package-prefix")
        private String packagePrefix;

        public String getName() {
            return name;
        }

        public void setNameForTests(String name) {
            this.name = name;
        }

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
        private String jdk;
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

        public String getJdk() {
            return jdk;
        }

        public void setJdkForTests(String jdk) {
            this.jdk = jdk;
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
        private String directory;
        @JsonProperty("also-log-to-console")
        private boolean alsoLogToConsole;

        public String getLevel() {
            return level;
        }

        public String getDirectory() {
            return directory;
        }

        public boolean getAlsoLogToConsole() {
            return alsoLogToConsole;
        }
    }


    public static class ExecutionPlanConfig {
        @JsonProperty("test-command")
        private String testCommand;
        @JsonProperty("ignore-failures")
        private boolean ignoreFailures;
        @JsonProperty("ignore-spoon-failures")
        private boolean ignoreSpoonFailures;
        @JsonProperty("num-test-runs")
        private int numTestRuns;
        @JsonProperty("energy-measurements")
        private EnergyMeasurementConfig energyMeasurements;

        public String getTestCommand() {
            return testCommand;
        }

        public boolean getIgnoreFailures() {
            return ignoreFailures;
        }

        public boolean getIgnoreSpoonFailures() {
            return ignoreSpoonFailures;
        }

        public int getNumTestRuns() {
            return numTestRuns;
        }

        public EnergyMeasurementConfig getEnergyMeasurements() {
            return energyMeasurements;
        }
    }

    public static class EnergyMeasurementConfig {
        private String tool;
        @JsonProperty("tool-version")
        private String toolVersion;
        @JsonProperty("release-url")
        private String releaseUrl;
        @JsonProperty("tool-path")
        private String toolPath;

        public String getTool() {
            return tool;
        }
        public String getToolVersion() {
            return toolVersion;
        }
        public String getReleaseUrl() {
            return releaseUrl;
        }
        public String getToolPath() {
            return toolPath;
        }

        @Override
        public String toString() {
            return "EnergyMeasurementConfig{" +
                    "tool='" + tool + '\'' +
                    ", toolVersion='" + toolVersion + '\'' +
                    ", releaseUrl='" + releaseUrl + '\'' +
                    ", toolPath='" + toolPath + '\'' +
                    '}';
        }
    }



    public static class BackendConfig {
        private String mode;
        @JsonProperty("server-timeout-seconds")
        private int serverTimeoutSeconds;
        @JsonProperty("server-path")
        private String serverPath;
        @JsonProperty("ssh-user")
        private String sshUser;
        @JsonProperty("ssh-host")
        private String sshHost;
        @JsonProperty("nb-check-server-start")
        private int nbCheckServerStart;
        @JsonProperty("server-log-path")
        private String serverLogPath;
        @JsonProperty("server-port")
        private int serverPort;
        @JsonProperty("server-host")
        private String serverHost;
        @JsonProperty("server-ready-path")
        private String serverReadyPath;
        @JsonProperty("endpoint")
        private String endpoint;

        public String getMode() {
            return mode;
        }

        public void setModeForTests(String mode) {
            this.mode = mode;
        }

        public String getServerPath() {
            return serverPath;
        }

        public void setServerPathForTests(String serverPath) {
            this.serverPath = serverPath;
        }

        public String getSshUser() {
            return sshUser;
        }

        public String getSshHost() {
            return sshHost;
        }

        public int getServerTimeoutSeconds() {
            return serverTimeoutSeconds;
        }

        public int getNbCheckServerStart() {
            return nbCheckServerStart;
        }

        public String getServerLogPath() {
            return serverLogPath;
        }

        public void setBackendLogPathForTests(String backendLogPath) {
            this.serverLogPath = backendLogPath;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPortForTests(int serverPort) {
            this.serverPort = serverPort;
        }

        public String getServerHost() {
            return serverHost;
        }

        public String getServerReadyPath() {
            return serverReadyPath;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
