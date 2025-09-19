package be.unamur.snail.utils.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class InitScriptGenerator {
    public File generate() throws IOException {
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
}
