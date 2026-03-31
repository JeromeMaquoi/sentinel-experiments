package be.unamur.snail.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JoularJXPropertiesValidationTest {
    private static Stream<String> providePropertiesFiles() {
        try {
            Stream<String> configFileNames = Files.list(Paths.get("."))
                .filter(path -> path.toFile().isFile())
                .map(path -> path.getFileName().toString())
                .filter(filename -> filename.matches("config-.*\\.yml"))  // Only non-wip files
                .sorted();

            return configFileNames.flatMap(configFileName -> {
                Pattern pattern = Pattern.compile("config-(.+)\\.yml");
                Matcher matcher = pattern.matcher(configFileName);

                if (matcher.find()) {
                    String projectName = matcher.group(1);

                    try {
                        return Files.walk(Paths.get("src/main/resources/build-files/" + projectName))
                                .filter(path -> path.toFile().isFile())
                                .filter(path -> path.getFileName().toString().equals("config.properties"))
                                .map(Path::toString)
                                .limit(1);
                    } catch (IOException e) {
                        System.out.println("Warning: No config.properties found for project " + projectName);
                        return Stream.empty();
                    }
                }
                return Stream.empty();
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover properties files", e);
        }
    }

    private Properties loadProperties(String filePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (IOException e) {
            fail("Failed to load properties file: " + filePath, e);
        }
        return props;
    }

    private static Stream<String> provideConfigFileProjectNames() {
        try {
            return Files.list(Paths.get("."))
                    .filter(path -> path.toFile().isFile())
                    .map(path -> path.getFileName().toString())
                    .filter(filename -> filename.matches("config-.*\\.yml"))
                    .map(filename -> {
                        Pattern pattern = Pattern.compile("config-(.+)\\.yml");
                        Matcher matcher = pattern.matcher(filename);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .sorted();
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover config files", e);
        }
    }

    @ParameterizedTest(name = "config-{0}.yml has corresponding config.properties")
    @MethodSource("provideConfigFileProjectNames")
    @DisplayName("Each config-<project-name>.yml has a corresponding config.properties")
    void everyConfigFileHasPropertiesFileTest(String projectName) {
        Path propertiesPath = Paths.get("src/main/resources/build-files/" +  projectName + "/config.properties");
        assertTrue(Files.exists(propertiesPath), "config.properties file not found for project '" + projectName + "' (expected at: " + propertiesPath.toAbsolutePath() + ")");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("save-runtime-data is true")
    void saveRuntimeDataTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("true", props.getProperty("save-runtime-data"), "save-runtime-data should be 'true' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("overwrite-runtime-data is false")
    void overwriteRuntimeDataTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("false", props.getProperty("overwrite-runtime-data"), "overwrite-runtime-data should be 'false' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("track-consumption-evolution is false")
    void trackConsumptionEvolutionTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("false", props.getProperty("track-consumption-evolution"), "track-consumption-evolution should be 'false' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("hide-agent-consumption is true")
    void hideAgentConsumptionTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("true", props.getProperty("hide-agent-consumption"), "hide-agent-consumption should be 'true' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("enable-call-trees-consumption is true")
    void enableCallTreesConsumptionTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("true", props.getProperty("enable-call-trees-consumption"), "enable-call-trees-consumption should be 'true' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("save-call-trees-runtime-data is true")
    void saveCallTreesRuntimeDataTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("true", props.getProperty("save-call-trees-runtime-data"), "save-call-trees-runtime-data should be 'true' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("overwrite-call-trees-runtime-data is false")
    void overwriteCallTreesRuntimeDataTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("false", props.getProperty("overwrite-call-trees-runtime-data"), "overwrite-call-trees-runtime-data should be 'false' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("application-server is false")
    void applicationServerTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("false", props.getProperty("application-server"), "application-server should be 'false' in " + filePath);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providePropertiesFiles")
    @DisplayName("vm-monitoring is false")
    void vmMonitoringTest(String filePath) {
        Properties props = loadProperties(filePath);
        assertEquals("false", props.getProperty("vm-monitoring"), "vm-monitoring should be 'false' in " + filePath);
    }
}
