package be.unamur.snail.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MavenPomModifierIT {
    private Path tempDir;
    private Path pomPath;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("maven");
        pomPath = tempDir.resolve("pom.xml");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void injectJavaAgentShouldAddArglineWhenMissingTest() throws Exception {
        String originalPom = """
           <project>
             <build>
               <plugins>
                 <plugin>
                   <artifactId>maven-surefire-plugin</artifactId>
                   <configuration></configuration>
                 </plugin>
               </plugins>
             </build>
           </project>
        """;
        Files.writeString(pomPath, originalPom);

        File backup = MavenPomModifier.injectJavaAgent(pomPath.toFile(), "/path/to/joular.jar");

        assertTrue(Files.exists(backup.toPath()), "Backup pom.xml.bah should be created");
        String backupContent = Files.readString(backup.toPath());
        assertTrue(backupContent.contains("<artifactId>maven-surefire-plugin</artifactId>"));

        String modifiedPom = Files.readString(pomPath);
        assertTrue(modifiedPom.contains("-javaagent:/path/to/joular.jar"), "pom.xml should contain the javaagent argument");
    }
}