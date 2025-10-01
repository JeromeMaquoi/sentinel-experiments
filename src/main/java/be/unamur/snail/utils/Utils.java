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

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static CompletedProcess runCommand(String command) throws IOException, InterruptedException {
        return runCommand(command, null);
    }

    public static CompletedProcess runCommand(String command, String cwd) throws IOException, InterruptedException {
        log.debug("Executing command `{}` in {}", command, cwd);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bash", "-c", command);
        builder.environment().put("PATH", "/usr/bin:/bin");

        if (cwd != null && !cwd.isEmpty()) {
            builder.directory(new File(cwd));
        }
        builder.redirectErrorStream(true);

        Process process = builder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        // Reader in a background thread
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Config config = Config.getInstance();
                    if (config.getProject().isShowProjectLogs()) {
                        log.debug("{}", line);
                    }
                    stdoutBuilder.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                log.error("Error reading process output", e);
            }
        });
        readerThread.start();

        Config config = Config.getInstance();
        boolean finished = process.waitFor(config.getCommandTimeout(), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new CommandTimedOutException(command);
        }

        readerThread.join();

        int returnCode = process.exitValue();
        return new CompletedProcess(command, returnCode, stdoutBuilder.toString(), "");
    }

    public record CompletedProcess(String args, int returnCode, String stdout, String stderr) {
    }
}
