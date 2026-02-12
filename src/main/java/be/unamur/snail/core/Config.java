package be.unamur.snail.core;

import be.unamur.snail.exceptions.ConfigNotLoadedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.List;

/**
 * Config class represents the configuration of the application, which is loaded from a YAML file. It contains nested static classes to represent different sections of the configuration, such as project details, repository information, logging settings, execution plan, and backend configuration. The Config class follows the singleton pattern to ensure that the configuration is loaded only once and can be accessed globally throughout the application.
 */
public class Config {
    /**
     * Singleton instance of the configuration. It is loaded once at the start of the application and can be accessed globally via getInstance().
     */
    private static Config instance;
    /**
     * Configuration for the project being analyzed, including its name, owner, subproject (if applicable), whether to show project logs, and package prefix for instrumentation.
     */
    private ProjectConfig project;
    /**
     * Configuration of the Git repository of the project being analyzed, including the URL, commit hash, target directory for cloning, JDK version, and flags for overwriting existing clones or copies.
     */
    private RepoConfig repo;
    /**
     * Configuration for logging during the execution of the pipeline, including the logging level, directory for log files, whether to also log to console, and whether to clear previous logs before starting a new execution.
     */
    private LogConfig log;
    /**
     * Path of the code to add to the project being analyzed. This code will be executed during the test suite execution of the instrumented project and is used to retrieve constructor data during this execution.
     */
    @JsonProperty("code-constructors-instrumentation-path")
    private String codeConstructorsInstrumentationPath;
    /**
     * Timeout in seconds for executing commands during the pipeline execution. This is used to prevent hanging processes and ensure that the pipeline can recover from failures in a timely manner. The default value is set to 120 seconds, but it can be overridden in the configuration file if needed.
     */
    @JsonProperty("command-time-out")
    private int commandTimeout = 120;
    /**
     * Configuration of the execution plan, which includes details about the test command to execute, whether to ignore test failures or Spoon failures, the number of test runs to perform, and the energy measurement configuration.
     */
    @JsonProperty("execution-plan")
    private ExecutionPlanConfig executionPlan;
    /**
     * Configuration of the backend, which includes details about the mode of operation (local or remote), server timeout, server path, SSH user and host for remote mode, number of checks for server start, server log path, server port and host, server ready path, and endpoint for communication with the backend.
     */
    private BackendConfig backend;

    /**
     * Returns the singleton instance of the configuration. If the configuration has not been loaded yet, it throws a ConfigNotLoadedException.
     * @return the singleton instance of the configuration
     */
    public static Config getInstance() {
        if (instance == null) {
            throw new ConfigNotLoadedException();
        }
        return instance;
    }

    public static void setInstanceForTests(Config config) {
        instance = config;
    }

    /**
     * Resets the singleton instance of the configuration.
     */
    public static void reset() {
        instance = null;
    }

    /**
     * Loads the configuration from a YAML file. It uses the Jackson library to parse the YAML file and populate the Config instance.
     * @param yamlPath the path to the YAML configuration file
     * @throws Exception if there is an error while loading the configuration, such as file not found or parsing errors
     */
    public static void load(String yamlPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        instance = mapper.readValue(new File(yamlPath), Config.class);
    }

    /**
     * Returns the project configuration, which includes details about the project being analyzed such as its name, owner, subproject, whether to show project logs, and package prefix for instrumentation.
     */
    public ProjectConfig getProject() {
        return project;
    }

    public void setProjectForTests(ProjectConfig project) {
        this.project = project;
    }

    /**
     * Returns the repository configuration, which includes details about the Git repository of the project being analyzed, such as the URL, commit hash, target directory for cloning, JDK version, and flags for overwriting existing clones or copies.
     */
    public RepoConfig getRepo() {
        return repo;
    }

    public void setRepoForTests(RepoConfig repo) {
        this.repo = repo;
    }

    /**
     * Returns the logging configuration, which includes details about the logging level, directory for log files, whether to also log to console, and whether to clear previous logs before starting a new execution.
     */
    public LogConfig getLog() {
        return log;
    }

    /**
     * Returns the path of the code to add to the project being analyzed. This code will be executed during the test suite execution of the instrumented project and is used to retrieve constructor data during this execution.
     */
    public String getCodeConstructorsInstrumentationPath() {
        return codeConstructorsInstrumentationPath;
    }

    public void setCodeConstructorsInstrumentationPathForTests(String codeConstructorsInstrumentationPath) {
        this.codeConstructorsInstrumentationPath = codeConstructorsInstrumentationPath;
    }

    /**
     * Returns the timeout in seconds for executing commands during the pipeline execution. This is used to prevent hanging processes and ensure that the pipeline can recover from failures in a timely manner.
     */
    public int getCommandTimeout() {
        return commandTimeout;
    }

    public void setTimeoutForTests(int timeout) {
        this.commandTimeout = timeout;
    }

    /**
     * Returns the execution plan configuration, which includes details about the test command to execute, whether to ignore test failures or Spoon failures, the number of test runs to perform, and the energy measurement configuration.
     */
    public ExecutionPlanConfig getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlanForTests(ExecutionPlanConfig executionPlan) {
        this.executionPlan = executionPlan;
    }

    /**
     * Returns the backend configuration, which includes details about the mode of operation (local or remote), server timeout, server path, SSH user and host for remote mode, number of checks for server start, server log path, server port and host, server ready path, and endpoint for communication with the backend.
     */
    public BackendConfig getBackend() {
        return backend;
    }

    public void setBackendForTests(BackendConfig backend) {
        this.backend = backend;
    }


    /**
     * ProjectConfig class represents the configuration related to the project being analyzed. It is a nested static class within the Config class and is used to encapsulate all project-related configuration details.
     */
    public static class ProjectConfig {
        /**
         * The name of the project being analyzed. This is a required field in the configuration and is used to identify the project in logs, during the instrumentation process and the energy measurement process. It is also used to determine the location of build files and other resources related to the project.
         */
        private String name;
        /**
         * The GitHub owner of the project.
         */
        private String owner;
        /**
         * The subproject or module within the main project that is being analyzed. This is an optional field and can be used to specify a particular submodule of a larger project, allowing for more targeted instrumentation and analysis. If not specified, the entire project will be analyzed.
         */
        @JsonProperty("sub-project")
        private String subProject;
        /**
         * A boolean flag indicating whether to show project logs during the execution of the pipeline.
         */
        @JsonProperty("show-project-logs")
        private boolean showProjectLogs;
        /**
         * An optional package prefix used to filter stacktraces during the instrumentation process.
         */
        @JsonProperty("package-prefix")
        private String packagePrefix;

        /**
         * Returns the name of the project being analyzed.
         */
        public String getName() {
            return name;
        }

        public void setNameForTests(String name) {
            this.name = name;
        }

        /**
         * Returns the GitHub owner of the project.
         */
        public String getOwner() {
            return owner;
        }

        public void setOwnerForTests(String owner) {
            this.owner = owner;
        }

        /**
         * Returns, if it exists, the subproject or module within the project that is being analyzed. This is used to specify a particular submodule of a larger project, allowing for more targeted instrumentation and analysis. If not specified, the entire project will be analyzed.
         */
        public String getSubProject() {
            return subProject;
        }

        public void setSubProjectForTests(String subProject) {
            this.subProject = subProject;
        }

        /**
         * Returns true if project logs should be shown during the execution of the pipeline, false otherwise.
         */
        public boolean isShowProjectLogs() {
            return showProjectLogs;
        }

        /**
         * Returns the package prefix used to filter stacktraces during the instrumentation process.
         */
        public String getPackagePrefix() {
            return packagePrefix;
        }
    }


    /**
     * RepoConfig class represents the configuration related to the Git repository of the project being analyzed. It is a nested static class within the Config class.
     */
    public static class RepoConfig {
        /**
         * URL of the Git repository of the project being analyzed.
         */
        private String url;
        /**
         * Specific commit hash that should be checked out in the repository for the analysis.
         */
        private String commit;
        /**
         * Target directory where the repository should be cloned.
         */
        @JsonProperty("target-dir")
        private String targetDir;
        /**
         * JDK version that is used by the analyzed project, so that this version is used during the instrumentation and the measurement process.
         */
        private String jdk;
        /**
         * Boolean flag indicating whether the repository should be re-cloned even if it already exists in the target directory.
         */
        @JsonProperty("overwrite-clone")
        private boolean overwriteClone;
        /**
         * Boolean flag indicating whether the copy of the analyzed project's directory should be overwritten or not.
         */
        @JsonProperty("overwrite-copy")
        private boolean overwriteCopy;

        /**
         * Returns the URL of the Git repository of the project being analyzed.
         */
        public String getUrl() {
            return url;
        }

        /**
         * Returns the specific commit hash that should be checked out in the repository, for the analysis.
         */
        public String getCommit() {
            return commit;
        }

        public void setCommitForTests(String commit) {
            this.commit = commit;
        }

        /**
         * Returns the target directory where the repository should be cloned.
         */
        public String getTargetDir() {
            return targetDir;
        }

        public void setTargetDirForTests(String targetDir) {
            this.targetDir = targetDir;
        }

        /**
         * Returns the JDK version that is used by the project being analyzed, so that this version is used during the instrumentation and the test execution process.
         */
        public String getJdk() {
            return jdk;
        }

        public void setJdkForTests(String jdk) {
            this.jdk = jdk;
        }

        /**
         * Returns true if the repository should be re-cloned even if it already exists in the target directory, false otherwise.
         */
        public boolean isOverwriteClone() {
            return overwriteClone;
        }

        public void setOverwriteForTests(boolean overwrite) {
            this.overwriteClone = overwrite;
        }

        /**
         * Returns true if the copy of the analyzed project's directory should be overwritten or not.
         */
        public boolean isOverwriteCopy() {
            return overwriteCopy;
        }

        public void setOverwriteCopyForTests(boolean overwriteCopy) {
            this.overwriteCopy = overwriteCopy;
        }
    }


    /**
     * LogConfig class represents the configuration related to logging during the execution of the pipeline. It is a nested class within the Config class.
     */
    public static class LogConfig {
        /**
         * Logging level to be used during the execution of the pipeline. The possible levels are TRACE, DEBUG, INFO, WARN and ERROR.
         */
        private String level;
        /**
         * Directory where log files should be stored during the execution of the pipeline.
         */
        private String directory;
        /**
         * Boolean indicating whether to also log to console during the execution of the pipeline, in addition to logging to files. If true, logs will be printed to the console as well as written to log files. If false, logs will only be written to files.
         */
        @JsonProperty("also-log-to-console")
        private boolean alsoLogToConsole;
        /**
         * Boolean indicating whether to clear previous logs before starting a new execution of the pipeline. If true, existing log files in the specified directory will be deleted before new logs are written. If false, new logs will be appended to existing log files.
         */
        @JsonProperty("clear-previous-logs")
        private boolean clearPreviousLogs;

        /**
         * Returns the logging level to be used during the execution of the pipeline. The possible levels are TRACE, DEBUG, INFO, WARN and ERROR.
         */
        public String getLevel() {
            return level;
        }

        /**
         * Returns the directory where log files should be stored during the execution of the pipeline.
         */
        public String getDirectory() {
            return directory;
        }

        /**
         * Returns true if logs should also be printed to the console during the execution of the pipeline, in addition to being written to log files, false otherwise.
         */
        public boolean getAlsoLogToConsole() {
            return alsoLogToConsole;
        }

        /**
         * Returns true if previous logs should be cleared before starting a new execution of the pipeline, false otherwise. If true, existing log files in the specified directory will be deleted before new logs are written. If false, new logs will be appended to existing log files.
         */
        public boolean getClearPreviousLogs() {
            return clearPreviousLogs;
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
        @JsonProperty("import-config")
        private ImportConfig importConfig;

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
        public ImportConfig getImportConfig() {
            return importConfig;
        }

        @Override
        public String toString() {
            return "EnergyMeasurementConfig{" +
                    "tool='" + tool + '\'' +
                    ", toolVersion='" + toolVersion + '\'' +
                    ", releaseUrl='" + releaseUrl + '\'' +
                    ", toolPath='" + toolPath + '\'' +
                    ", importConfig=" + importConfig +
                    '}';
        }
    }



    public static class ImportConfig {
        private List<String> scopes;
        @JsonProperty("measurement-types")
        private List<String> measurementTypes;
        @JsonProperty("monitoring-types")
        private List<String> monitoringTypes;

        public List<String> getScopes() {
            return scopes;
        }

        public List<String> getMeasurementTypes() {
            return measurementTypes;
        }

        public List<String> getMonitoringTypes() {
            return monitoringTypes;
        }

        @Override
        public String toString() {
            return "ImportConfig{" +
                    "scopes=" + scopes +
                    ", measurementTypes=" + measurementTypes +
                    ", monitoringTypes=" + monitoringTypes +
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

        public void setServerHostForTests(String serverHost) {
            this.serverHost = serverHost;
        }

        public String getServerReadyPath() {
            return serverReadyPath;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
