package be.unamur.snail.utils;

import be.unamur.snail.config.Config;
import be.unamur.snail.exceptions.CommandTimedOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static CompletedProcess runCommand(String command) throws IOException, InterruptedException {
        return runCommand(command, null);
    }

    public static CompletedProcess runCommand(String command, String cwd) throws IOException, InterruptedException {
        log.info("Executing command: {}", command);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bash", "-c", command);
        builder.environment().put("PATH", "/usr/bin:/bin");

        if (cwd != null && !cwd.isEmpty()) {
            builder.directory(new File(cwd));
        }
        builder.redirectErrorStream(false);

        Process process = builder.start();

        Config config = Config.getInstance();
        boolean finished = process.waitFor(config.getTimeout(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new CommandTimedOutException(command);
        }

        String stdout;
        String stderr;
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            stdout = outReader.lines().collect(Collectors.joining(System.lineSeparator()));
            stderr = errReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }

        int returnCode = process.exitValue();

        if (returnCode != 0) {
            log.error("Command failed with return code: {}", returnCode);
            log.error("stdout: {}", stdout);
            log.error("stderr: {}", stderr);
        }
        return new CompletedProcess(command, returnCode, stdout, stderr);
    }

    public record CompletedProcess(String args, int returnCode, String stdout, String stderr) {
    }
}
