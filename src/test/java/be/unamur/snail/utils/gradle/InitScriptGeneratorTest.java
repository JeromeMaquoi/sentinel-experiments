package be.unamur.snail.utils.gradle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class InitScriptGeneratorTest {
    private InitScriptGenerator generator = new InitScriptGenerator();
    private File tempFile;

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldCreateInitScriptFileWithJavaAgentAndIterationIdScriptTest() throws IOException {
        String energyToolPath = "/path/to/joularjx.jar";
        tempFile = generator.generateGradleJavaAgentAndIterationIdInitScript(energyToolPath);
        assertNotNull(tempFile);
        assertTrue(tempFile.exists(), "Init script file should exist");
        assertTrue(tempFile.length() > 0, "Init script file length should be greater than 0");

        String content = Files.readString(tempFile.toPath());
        assertTrue(content.contains("systemProperty 'ITERATION_ID'"), "Script should set systemProperty 'ITERATION_ID'");
        assertTrue(content.contains("-javaagent:" + energyToolPath), "Script should set javaagent agent path");
        assertTrue(content.contains("tasks.withType(Test)"), "Script should configure all test tasks");
    }
}