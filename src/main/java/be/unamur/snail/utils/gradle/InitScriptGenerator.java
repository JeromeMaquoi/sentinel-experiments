package be.unamur.snail.utils.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class InitScriptGenerator {
    /**
     * Creates
     * @return
     * @throws IOException
     */
    public File generateClasspathInitScript() throws IOException {
        // Create a temporary init script with the task definition
        File initScript = File.createTempFile("sentinel-export-classpath", ".gradle");
        String gradleTaskContent = """
        allprojects {
            tasks.register("exportRuntimeClasspath") {
                doLast {
                    def f = new File(project.projectDir, "classpath.txt")
                    if (configurations.findByName("runtimeClasspath") != null) {
                        f.text = configurations.runtimeClasspath.files.collect { it.absolutePath }.join('\\n')
                    } else {
                        f.text = ""
                    }
                }
            }
        }
        """;
        Files.writeString(initScript.toPath(), gradleTaskContent);
        return initScript;
    }

    /**
     * Create a temp gradle file with init script in it to configure
     * the project to show logs in terminal during execution
     * @return the temporary file where the script has been
     * created
     * @throws IOException if there is a problem during the creation of
     * the file
     */
    public File generateShowLogsInitScriptForGradle() throws IOException {
        String script = """
                allprojects {
                    tasks.withType(Test).configureEach {
                        testLogging {
                            showStandardStreams = true
                            events 'passed', 'failed', 'skipped'
                            exceptionFormat 'full'
                        }
                        // Forward the packagePrefix system property to the test JVM
                        if (System.getProperty("packagePrefix") != null) {
                            systemProperty "packagePrefix", System.getProperty("packagePrefix")
                        }
                        // Forward the apiUrl system property to the test JVM
                        if (System.getProperty("apiUrl") != null) {
                            systemProperty "apiUrl", System.getProperty("apiUrl")
                        }
                    }
                }
                """;
        File tempFile = File.createTempFile("test-logging", ".gradle");
        Files.writeString(tempFile.toPath(), script);
        return tempFile;
    }

    /**
     * Create a temp file with init script in it to configure the
     * project to use JoularJX as java agent during the test phase
     * as well as using system property ITERATION_ID for the
     * energy measurements runs
     * @param energyToolPath the path of the energy tool used
     * @return the new init script file created
     * @throws IOException if there is a problem during the creation
     * of the file
     */
    public File generateGradleJavaAgentAndIterationIdInitScript(String energyToolPath) throws IOException {
        File initScript = File.createTempFile("sentinel-inject-agent", ".gradle");
        String content = String.format("""
                allprojects {
                    tasks.withType(Test).configureEach { test ->
                        test.systemProperty 'ITERATION_ID', project.findProperty('ITERATION_ID')
                        test.jvmArgs += ["-javaagent:%s"]
                    }
                }
                """, energyToolPath);
        Files.writeString(initScript.toPath(), content);
        return initScript;
    }

    public File createMavenArgLineFile(String energyToolPath) {
        // TODO
        return null;
    }
}
