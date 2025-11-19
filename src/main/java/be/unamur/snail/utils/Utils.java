package be.unamur.snail.utils;

import be.unamur.snail.core.Config;
import be.unamur.snail.exceptions.CommandTimedOutException;
import be.unamur.snail.logging.PipelineLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static PipelineLogger log;

    public static void setPipelineLogger(PipelineLogger logger) {
        log = logger;
    }

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
                        log.info("{}", line);
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

    public static String createEndpointURL(Config config, String endpoint) {
        String host = config.getBackend().getServerHost();
        int port = config.getBackend().getServerPort();
        return endpoint.startsWith("/") ?
                String.format("http://%s:%d%s", host, port, endpoint) :
                String.format("http://%s:%d/%s", host, port, endpoint);
    }

    public static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
