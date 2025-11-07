package be.unamur.snail.core;

import be.unamur.snail.exceptions.ConfigNotLoadedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        Config.reset();
    }

    @Test
    void loadConfigFromYamlTest() throws Exception {
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              sub-project: "sub/module"
              show-project-logs: true
              package-prefix: "org.springframework"
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: "/tmp/repo"
              jdk: "17-tem"
              overwrite: true
            log:
              level: "DEBUG"
            command-time-out: 150
            execution-plan:
              test-command: "mvn test"
              ignore-failures: true
              ignore-spoon-failures: false
              num-test-runs: 5
              energy-measurements:
                tool: "jRAPL"
                tool-version: "1.0.0"
                release-url: "https://example.com/jrapl.zip"
                tool-path: "/tools/jrapl"
                import-config:
                    scopes: ["app", "all"]
                    measurement-types: ["runtime", "total"]
                    monitoring-types: ["methods", "calltrees"]
            backend:
              mode: dev
              server-timeout-seconds: 60
              server-path: "/usr/local/server"
              ssh-user: "tester"
              ssh-host: "host.example.com"
              nb-check-server-start: 3
              server-log-path: "/var/log/server.log"
              server-port: 8080
              server-host: "localhost"
              server-ready-path: "/ready"
              endpoint: "/api"
        """);
        Config.load(yaml.toString());
        Config config = Config.getInstance();
        assertNotNull(config);

        // Project config
        assertEquals("sub/module", config.getProject().getSubProject());
        assertTrue(config.getProject().isShowProjectLogs());
        assertEquals("org.springframework", config.getProject().getPackagePrefix());

        // Repo config
        assertEquals("https://example.com/repo.git", config.getRepo().getUrl());
        assertEquals("123abc", config.getRepo().getCommit());
        assertEquals("/tmp/repo", config.getRepo().getTargetDir());
        assertEquals("17-tem", config.getRepo().getJdk());
        assertTrue(config.getRepo().isOverwrite());

        // Log config
        assertEquals("DEBUG", config.getLog().getLevel());

        // Command timeout
        assertEquals(150, config.getCommandTimeout());

        // Execution plan config
        assertEquals("mvn test", config.getExecutionPlan().getTestCommand());
        assertTrue(config.getExecutionPlan().getIgnoreFailures());
        assertFalse(config.getExecutionPlan().getIgnoreSpoonFailures());
        assertEquals(5, config.getExecutionPlan().getNumTestRuns());

        Config.EnergyMeasurementConfig em = config.getExecutionPlan().getEnergyMeasurements();
        assertNotNull(em);
        assertEquals("jRAPL", em.getTool());
        assertEquals("1.0.0", em.getToolVersion());
        assertEquals("https://example.com/jrapl.zip", em.getReleaseUrl());
        assertEquals("/tools/jrapl", em.getToolPath());

        Config.ImportConfig importConfig = em.getImportConfig();
        assertNotNull(importConfig);
        assertEquals(List.of("app", "all"), importConfig.getScopes());
        assertEquals(List.of("runtime", "total"), importConfig.getMeasurementTypes());
        assertEquals(List.of("methods", "calltrees"), importConfig.getMonitoringTypes());

        // Backend config
        assertEquals("dev", config.getBackend().getMode());
        assertEquals(60, config.getBackend().getServerTimeoutSeconds());
        assertEquals("/usr/local/server", config.getBackend().getServerPath());
        assertEquals("tester", config.getBackend().getSshUser());
        assertEquals("host.example.com", config.getBackend().getSshHost());
        assertEquals(3, config.getBackend().getNbCheckServerStart());
        assertEquals("/var/log/server.log", config.getBackend().getServerLogPath());
        assertEquals(8080, config.getBackend().getServerPort());
        assertEquals("localhost", config.getBackend().getServerHost());
        assertEquals("/ready", config.getBackend().getServerReadyPath());
        assertEquals("/api", config.getBackend().getEndpoint());
    }

    @Test
    void getInstanceFailsIfNotLoadedTest() {
        assertThrows(ConfigNotLoadedException.class, Config::getInstance);
    }

    @Test
    void settersForTestsShouldOverrideValuesTest() {
        Config config = new Config();
        Config.setInstanceForTests(config);

        // Project
        Config.ProjectConfig project = new Config.ProjectConfig();
        project.setSubProjectForTests("test-sub-project");
        config.setProjectForTests(project);

        // Repo
        Config.RepoConfig repo = new Config.RepoConfig();
        repo.setCommitForTests("123abc");
        repo.setTargetDirForTests("/tmp/repo");
        repo.setJdkForTests("17-tem");
        repo.setOverwriteForTests(true);
        config.setRepoForTests(repo);

        // Backend
        Config.BackendConfig backend = new Config.BackendConfig();
        backend.setModeForTests("dev");
        backend.setServerPathForTests("/usr/local/server");
        backend.setBackendLogPathForTests("/var/log/server.log");
        backend.setServerPortForTests(8080);

        config.setExecutionPlanForTests(new Config.ExecutionPlanConfig());
        config.setBackendForTests(backend);
        config.setCodeConstructorsInstrumentationPathForTests("/code/instrumentation/path");
        config.setTimeoutForTests(42);

        // Assertions
        assertEquals("test-sub-project", config.getProject().getSubProject());
        assertEquals("123abc", config.getRepo().getCommit());
        assertEquals("/tmp/repo", config.getRepo().getTargetDir());
        assertEquals("17-tem", config.getRepo().getJdk());
        assertTrue(config.getRepo().isOverwrite());
        assertEquals("dev", config.getBackend().getMode());
        assertEquals("/usr/local/server", config.getBackend().getServerPath());
        assertEquals("/var/log/server.log", config.getBackend().getServerLogPath());
        assertEquals(8080, config.getBackend().getServerPort());
        assertEquals("/code/instrumentation/path", config.getCodeConstructorsInstrumentationPath());
        assertEquals(42, config.getCommandTimeout());
    }
}