package be.unamur.snail.sentinelbackend;

import be.unamur.snail.config.Config;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.PortAlreadyInUseException;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DevBackendServiceManager implements BackendServiceManager {
    private static final Logger log = LoggerFactory.getLogger(DevBackendServiceManager.class);

    private final CommandRunner runner;
    private final String backendPath;
    private final int nbCheckServerStart;
    private final int delayMs;
    private String sessionName = "sentinel-backend";

    public DevBackendServiceManager(CommandRunner runner, String backendPath, int nbCheckServerStart, int delayMs) {
        this.runner = runner;
        this.backendPath = backendPath;
        this.nbCheckServerStart = nbCheckServerStart;
        this.delayMs = delayMs;
    }

    @Override
    public boolean startBackend() throws IOException, InterruptedException {
        Config config = Config.getInstance();
        int backendPort = config.getDatabase().getBackendPort();
        if (backendPort == 0) {
            throw new MissingConfigKeyException("database.backend-port");
        }

        if (isPortInUse(backendPort)) {
            throw new PortAlreadyInUseException(backendPort);
        }

        if (isScreenSessionRunning(sessionName)) {
            log.info("Screen session already running");
            return true;
        }

        String command = createCompleteCommand(backendPath);
        runner.run(command);
        return isServerRunning();
    }

    public boolean isPortInUse(int port) {
        try {
            Utils.CompletedProcess result = runner.run("lsof -i:" + port);
            return result.returnCode() == 0 && !result.stdout().isBlank();
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public String createCompleteCommand(String backendPath) {
        Config config = Config.getInstance();
        int backendTimeout = config.getDatabase().getBackendTimeoutSeconds();

        if (config.getDatabase().getBackendLogPath() == null) {
            throw new MissingConfigKeyException("database.backend-log-path");
        }

        String cdScript = "cd " + backendPath;
        String makeScriptExecutable = "chmod +X start-server.sh";

        String logPath = config.getDatabase().getBackendLogPath();
        String logOutputScript = "> " + logPath + " 2>&1";

        String pluginsDirectory = "PLUGINS_DIRECTORY=" + backendPath + "plugins";

        String scriptCommand = cdScript + " && " + makeScriptExecutable + " && " + pluginsDirectory + " ./start-server.sh " + backendTimeout + " " + logOutputScript;

        return "screen -dmS " + sessionName + " bash -c \"" + scriptCommand + "\"";
    }

    public boolean isServerRunning() throws IOException, InterruptedException {
        Path readyFile = Paths.get("/tmp/backend-ready");
        for (int i = 0; i < nbCheckServerStart; i++) {
            if (Files.exists(readyFile)) {
                String status = Files.readString(readyFile).trim();
                Files.deleteIfExists(readyFile);
                return status.equals("READY");
            }
            log.info("Waiting for backend to be ready... (attempt {}/{})", i + 1, nbCheckServerStart);
            Thread.sleep(delayMs);
        }
        return false;
    }

    /**
     * Verifies if a screen session is already running for a given session name
     * @param sessionName name of the session
     * @return true if the session exists, false otherwise
     */
    public boolean isScreenSessionRunning(String sessionName) {
        try {
            Utils.CompletedProcess result = runner.run("screen -ls | grep " + sessionName);
            return result.returnCode() == 0 && !result.stdout().isBlank();
        } catch (IOException | InterruptedException e) {
            log.warn("Could not check screen sessions");
            return false;
        }
    }
}
