package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.UnknownProjectBuildException;
import be.unamur.snail.utils.MavenPomModifier;
import be.unamur.snail.utils.ProjectTypeDetector;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UpdateBuildConfigurationStageTest {
    private InitScriptGenerator initScriptGenerator;
    private Config config;
    private ProjectTypeDetector projectTypeDetector;
    private Context context;
    private UpdateBuildConfigurationStage stage;

    @BeforeEach
    void setUp() {
        initScriptGenerator = mock(InitScriptGenerator.class);
        config = mock(Config.class);
        projectTypeDetector = mock(ProjectTypeDetector.class);
        context = new Context();
        context.setEnergyToolPath("/path/to/joular.jar");

        stage = new UpdateBuildConfigurationStage(initScriptGenerator, config, projectTypeDetector);
    }

    @Test
    void executeShouldThrowExceptionIfRepoPathIsNullTest() {
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeShouldThrowExceptionIfEnergyToolPathIsNullTest() {
        context.setRepoPath("/some/path");
        context.setEnergyToolPath(null);
        assertThrows(MissingContextKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeShouldThrowExceptionIfProjectBuildNotRecognizedTest() {
        context.setRepoPath("/some/path");
        when(projectTypeDetector.isGradleProject(any())).thenReturn(false);
        when(projectTypeDetector.isMavenProject(any())).thenReturn(false);
        assertThrows(UnknownProjectBuildException.class, () -> stage.execute(context));
    }

    @Test
    void executeShouldGenerateGradleInitScriptWhenProjectIsGradleTest() throws Exception {
        File fakeRepo = new File("fakeRepo").getCanonicalFile();
        File fakeInit = new File("init.gradle");
        context.setRepoPath(fakeRepo.getAbsolutePath());
        when(projectTypeDetector.isGradleProject(eq(fakeRepo))).thenReturn(true);
        when(initScriptGenerator.generateGradleJavaAgentAndIterationIdInitScript(anyString())).thenReturn(fakeInit);

        stage.execute(context);

        assertEquals(fakeInit, context.getInitScript());
        verify(initScriptGenerator).generateGradleJavaAgentAndIterationIdInitScript("/path/to/joular.jar");
    }

    @Test
    void executeShouldInjectJavaAgentWhenProjectIsMavenTest() throws Exception {
        Path repoPath = Files.createTempDirectory("fakeRepo");
        Path pomPath = repoPath.resolve("pom.xml");
        Files.writeString(pomPath, """
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
    """);

        when(projectTypeDetector.isGradleProject(eq(repoPath.toFile()))).thenReturn(false);
        when(projectTypeDetector.isMavenProject(eq(repoPath.toFile()))).thenReturn(true);
        context.setRepoPath(repoPath.toString());

        stage.execute(context);

        String modifiedPom = Files.readString(pomPath);
        assertTrue(modifiedPom.contains("-javaagent:/path/to/joular.jar"),
                "pom.xml should contain the javaagent argument");

        Path backupPom = repoPath.resolve("pom.xml.bak");
        assertTrue(Files.exists(backupPom), "Backup pom.xml should be created");

        assertEquals(backupPom.toFile(), context.getBackupPom(),
                "Context should reference the backup pom file");
    }
}