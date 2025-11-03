package be.unamur.snail.utils;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.CommandTimedOutException;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.logging.PipelineLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @TempDir
    private Path tempDir;

    private PipelineLogger logger;

    @BeforeEach
    void setUp() throws Exception {
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              sub-project: "sub/module"
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: "/tmp/repo"
            log:
              level: "DEBUG"
        """);
        Config.load(yaml.toString());

        logger = new ConsolePipelineLogger(Utils.class);
        Utils.setPipelineLogger(logger);
    }

    @AfterEach
    void tearDown() {
        Config.reset();
    }

    @Test
    void runCommandSuccessEchoTest() throws IOException, InterruptedException {
        String command = "echo Hello World";
        Utils.CompletedProcess result = Utils.runCommand(command);
        assertThat(result.returnCode()).isZero();
        assertThat(result.stdout()).contains("Hello World");
        assertThat(result.stderr()).isBlank();
    }

    @Test
    void runCommandInWorkingDirectoryTest() throws IOException, InterruptedException {
        String command = "ls";
        String cwd = System.getProperty("user.dir");
        Utils.CompletedProcess result = Utils.runCommand(command, cwd);
        assertThat(result.returnCode()).isZero();
        assertThat(result.stdout()).contains("src");
    }

    @Test
    void runCommandTimeoutTest() {
        String command = "sleep 2";
        Config.getInstance().setTimeoutForTests(1);

        Exception ex = assertThrows(CommandTimedOutException.class, () -> Utils.runCommand(command));
        assertThat(ex.getMessage()).contains("Command timed out");
    }

    @Test
    void runCommandShouldReturnNonZeroExitCodeTest() throws IOException, InterruptedException {
        String failingCommand = "false";
        Utils.CompletedProcess result = Utils.runCommand(failingCommand);
        assertEquals(failingCommand, result.args());
        assertTrue(result.stdout().isEmpty(), "stdout should be empty for `false` command");
        assertTrue(result.stderr().isEmpty(), "stderr should be empty for `false` command");
    }
}