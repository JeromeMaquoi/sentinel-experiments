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


    /**
     * ExecutionPlanConfig class represents the configuration related to the execution plan of the pipeline, for the energy measurements or the instrumentation process. It is a nested static class within the Config class.
     */
    public static class ExecutionPlanConfig {
        /**
         * Command to execute the test suite of the project being analyzed.
         */
        @JsonProperty("test-command")
        private String testCommand;
        /**
         * Boolean indicating whether to ignore test failures during the execution of the pipeline. If true, the pipeline will continue executing even if some tests fail. If false, the pipeline will stop if any test fails.
         */
        @JsonProperty("ignore-failures")
        private boolean ignoreFailures;
        /**
         * Boolean indicating whether to ignore Spoon failures during the execution of the pipeline. If true, the pipeline will continue executing even if there are failures related to Spoon, during the instrumentation module. If false, the pipeline will stop if any Spoon-related failure occurs.
         */
        @JsonProperty("ignore-spoon-failures")
        private boolean ignoreSpoonFailures;
        /**
         * Number of times to execute the test suite of the project being analyzed. This is used to perform multiple runs of the test suite, which can be useful for obtaining more reliable energy measurements by averaging results across multiple runs.
         */
        @JsonProperty("num-test-runs")
        private int numTestRuns;
        /**
         * Configuration related to energy measurements, which includes details about the tool to use for energy measurements, its version, release URL, path to the tool, and configuration for importing measurement data into the backend.
         */
        @JsonProperty("energy-measurements")
        private EnergyMeasurementConfig energyMeasurements;

        /**
         * Returns the command to execute the test suite of the project being analyzed.
         * @return the command to execute the test suite
         */
        public String getTestCommand() {
            return testCommand;
        }

        /**
         * Returns true if test failures should be ignored during the execution of the pipeline, false otherwise. If true, the pipeline will continue executing even if some tests fail. If false, the pipeline will stop if any test fails.
         * @return true if test failures should be ignored, false otherwise
         */
        public boolean getIgnoreFailures() {
            return ignoreFailures;
        }

        /**
         * Returns true if Spoon failures should be ignored during the execution of the pipeline, false otherwise. If true, the pipeline will continue executing even if there are failures related to Spoon, during the instrumentation module. If false, the pipeline will stop if any Spoon-related failure occurs.
         * @return true if Spoon failures should be ignored, false otherwise
         */
        public boolean getIgnoreSpoonFailures() {
            return ignoreSpoonFailures;
        }

        /**
         * Returns the number of times to execute the test suite of the project being analyzed. This is used to perform multiple runs of the test suite, which can be useful for obtaining more reliable energy measurements by averaging results across multiple runs.
         * @return the number of times to execute the test suite
         */
        public int getNumTestRuns() {
            return numTestRuns;
        }

        /**
         * Returns the configuration related to energy measurements, which includes details about the tool to use for energy measurements, its version, release URL, path to the tool, and configuration for importing measurement data into the backend.
         * @return the energy measurement configuration
         */
        public EnergyMeasurementConfig getEnergyMeasurements() {
            return energyMeasurements;
        }
    }

    /**
     * EnergyMeasurementConfig class represents the configuration related to how energy measurements are made, with which tool, and which results are imported to the backend. It is a nested class within the ExecutionPlanConfig class.
     */
    public static class EnergyMeasurementConfig {
        /**
         * The name of the tool to use for energy measurements. This is used to specify which tool should be used to perform energy measurements during the execution of the pipeline. The tool specified here will be used to collect energy consumption data while running the test suite of the project being analyzed.
         */
        private String tool;
        /**
         * The version of the tool to use for energy measurements
         */
        @JsonProperty("tool-version")
        private String toolVersion;
        /**
         * The release URL of the tool to use for energy measurements, used to retrieve the tool if it is not already available locally.
         */
        @JsonProperty("release-url")
        private String releaseUrl;
        /**
         * The path where the tool to use for energy measurements is located. If the tool is not already present at this path, it can be downloaded from the specified release URL and stored at this location for use during energy measurements.
         */
        @JsonProperty("tool-path")
        private String toolPath;
        /**
         * Configuration for importing measurement data into the backend, specifying which data generated by the energy measurement tool should be imported into the backend.
         */
        @JsonProperty("import-config")
        private ImportConfig importConfig;

        /**
         * Returns the name of the tool to use for energy measurements. This is used to specify which tool should be used to perform energy measurements during the execution of the pipeline. The tool specified here will be used to collect energy consumption data while running the test suite of the project being analyzed.
         * @return the name of the tool to use for energy measurements
         */
        public String getTool() {
            return tool;
        }

        /**
         * Returns the version of the tool to use for energy measurements.
         * @return the version of the tool
         */
        public String getToolVersion() {
            return toolVersion;
        }

        /**
         * Returns the release URL of the tool to use for energy measurements, used to retrieve the tool if it is not already available locally.
         * @return the release URL of the tool
         */
        public String getReleaseUrl() {
            return releaseUrl;
        }

        /**
         * Returns the path where the tool to use for energy measurements is located.
         * @return the path to the tool for energy measurements
         */
        public String getToolPath() {
            return toolPath;
        }

        /**
         * Returns the configuration for importing measurement data into the backend, specifying which data generated by the energy measurement tool should be imported into the backend.
         * @return the configuration for importing measurement data into the backend
         */
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


    /**
     * ImportConfig class represents the configuration for selecting which data generated by the energy measurement tool should be imported into the backend. It is a nested class within the EnergyMeasurementConfig class.
     */
    public static class ImportConfig {
        /**
         * List of scopes to import into the backend. A scope is either "all" or "app", and represents the level of granularity of the data to import. See the docs of JoularJX for more details about scopes.
         */
        private List<String> scopes;
        /**
         * List of measurement types to import into the backend. A measurement type is either "runtime" or "total", and represents either the power consumption data or the energy consumption data generated by JoularJX. See the docs of JoularJX for more details about measurement types.
         */
        @JsonProperty("measurement-types")
        private List<String> measurementTypes;
        /**
         * List of monitoring types to import into the backend. A monitoring type is either "calltrees" or "methods", and represents the type of data related to method calls generated by JoularJX. See the docs of JoularJX for more details about monitoring types.
         */
        @JsonProperty("monitoring-types")
        private List<String> monitoringTypes;


        /**
         * Returns the list of scopes to import into the backend. A scope is either "all" or "app", and represents the level of granularity of the data to import. See the docs of JoularJX for more details about scopes.
         * @return the list of scopes to import into the backend
         */
        public List<String> getScopes() {
            return scopes;
        }

        /**
         * Returns the list of measurement types to import into the backend. A measurement type is either "runtime" or "total", and represents either the power consumption data or the energy consumption data generated by JoularJX. See the docs of JoularJX for more details about measurement types.
         * @return the list of measurement types to import into the backend
         */
        public List<String> getMeasurementTypes() {
            return measurementTypes;
        }

        /**
         * Returns the list of monitoring types to import into the backend. A monitoring type is either "calltrees" or "methods", and represents the type of data related to method calls generated by JoularJX. See the docs of JoularJX for more details about monitoring types.
         * @return the list of monitoring types to import into the backend
         */
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


    /**
     * BackendConfig class represents the configuration related to the backend that handles the database where all the data generated during the execution of the pipeline is stored.
     */
    public static class BackendConfig {
        /**
         * Either "dev" or "prod", representing whether the backend is running in development mode or production mode.
         */
        private String mode;
        /**
         * Timeout in seconds for the backend server to start and be ready to receive requests. This is used to ensure that the pipeline waits for the backend server to be fully operational before attempting any request. After this timeout, the server writes a READY in a tmp file to indicate that it is ready, and the pipeline checks for the existence of this file to determine if the server is ready. If the server is not ready within this timeout, it writes FAILED to the tmp file, and the pipeline stops.
         */
        @JsonProperty("server-timeout-seconds")
        private int serverTimeoutSeconds;
        /**
         * Path to the "sentinel-backend" directory project, which contains the code of the backend server. This is used to start the backend server during the execution of the pipeline, by running the relevant command in this directory. This property is only used in dev mode.
         */
        @JsonProperty("server-path")
        private String serverPath;
        /**
         * SSH user to connect to the remote server where the backend is running, for the "prod" mode.
         */
        @JsonProperty("ssh-user")
        private String sshUser;
        /**
         * SSH host to connect to the remote server where the backend is running, for the "prod" mode.
         */
        @JsonProperty("ssh-host")
        private String sshHost;
        /**
         * Number of checks to perform to verify if the backend server has started and is ready to receive requests.
         */
        @JsonProperty("nb-check-server-start")
        private int nbCheckServerStart;
        /**
         * Path of a log file where the backend server writes its logs during startup and execution.
         */
        @JsonProperty("server-log-path")
        private String serverLogPath;
        /**
         * Port on which the backend server is running, used to send requests to the server during the execution of the pipeline.
         */
        @JsonProperty("server-port")
        private int serverPort;
        /**
         * Host on which the backend server is running, used to send requests to the server during the execution of the pipeline.
         */
        @JsonProperty("server-host")
        private String serverHost;
        /**
         * Path to the log file of the backend server, used to check if the server has started successfully or if it has failed to start. It's in this tmp file that the backend writes READY or FAILED to indicate whether it has started successfully or not.
         */
        @JsonProperty("server-ready-path")
        private String serverReadyPath;
        /**
         * Endpoint for the instrumentation module to send all the generated data to the backend server through a rest API.
         */
        @JsonProperty("endpoint")
        private String endpoint;

        /**
         * Returns the mode of operation for the backend, which can be either "dev" or "prod"
         * @return the mode of operation for the backend
         */
        public String getMode() {
            return mode;
        }

        public void setModeForTests(String mode) {
            this.mode = mode;
        }

        /**
         * Returns the timeout in seconds for the backend server to start and be ready to receive requests. This is used to ensure that the pipeline waits for the backend server to be fully operational before attempting any request. After this timeout, the server writes a READY in a tmp file to indicate that it is ready, and the pipeline checks for the existence of this file to determine if the server is ready. If the server is not ready within this timeout, it writes FAILED to the tmp file, and the pipeline stops.
         * @return the timeout in seconds for the backend server to start and be ready to receive requests
         */
        public String getServerPath() {
            return serverPath;
        }

        public void setServerPathForTests(String serverPath) {
            this.serverPath = serverPath;
        }

        /**
         * Returns the SSH user to connect to the remote server where the backend is running, for the "prod" mode.
         * @return the SSH user for connecting to the remote server
         */
        public String getSshUser() {
            return sshUser;
        }

        /**
         * Returns the SSH host to connect to the remote server where the backend is running, for the "prod" mode.
         * @return the SSH host for connecting to the remote server
         */
        public String getSshHost() {
            return sshHost;
        }

        /**
         * Returns the timeout in seconds for the backend server to start and be ready to receive requests. This is used to ensure that the pipeline waits for the backend server to be fully operational before attempting any request. After this timeout, the server writes a READY in a tmp file to indicate that it is ready, and the pipeline checks for the existence of this file to determine if the server is ready. If the server is not ready within this timeout, it writes FAILED to the tmp file, and the pipeline stops.
         * @return the timeout in seconds for the backend server to start and be ready to receive requests
         */
        public int getServerTimeoutSeconds() {
            return serverTimeoutSeconds;
        }

        /**
         * Returns the number of checks to perform to verify if the backend server has started and is ready to receive requests.
         * @return the number of checks
         */
        public int getNbCheckServerStart() {
            return nbCheckServerStart;
        }

        /**
         * Returns the path of a log file where the backend server writes its logs during startup and execution.
         * @return the path of the backend server log file
         */
        public String getServerLogPath() {
            return serverLogPath;
        }

        public void setBackendLogPathForTests(String backendLogPath) {
            this.serverLogPath = backendLogPath;
        }

        /**
         * Returns the port on which the backend server is running, used to send requests to the server during the execution of the pipeline.
         * @return the port of the backend server
         */
        public int getServerPort() {
            return serverPort;
        }

        public void setServerPortForTests(int serverPort) {
            this.serverPort = serverPort;
        }

        /**
         * Returns the host on which the backend server is running, used to send requests to the server during the execution of the pipeline.
         * @return the host of the backend server
         */
        public String getServerHost() {
            return serverHost;
        }

        public void setServerHostForTests(String serverHost) {
            this.serverHost = serverHost;
        }

        /**
         * Returns the path to the log file of the backend server, used to check if the server has started successfully or if it has failed to start. It's in this tmp file that the backend writes READY or FAILED to indicate whether it has started successfully or not.
         * @return the path to the log file of the backend server for checking if it has started successfully
         */
        public String getServerReadyPath() {
            return serverReadyPath;
        }

        /**
         * Returns the endpoint for the instrumentation module to send all the generated data to the backend server through a rest API.
         * @return the endpoint for communication with the backend server
         */
        public String getEndpoint() {
            return endpoint;
        }
    }
}
