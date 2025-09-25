package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.ServerNotStartedException;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrepareDatabaseStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(PrepareDatabaseStage.class);

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String mode = config.getDatabase().getMode();
        if (mode == null || mode.isEmpty()) {
            throw new MissingConfigKeyException("database.mode");
        }
        String backendPath = config.getDatabase().getBackendPath();
        if (backendPath == null || backendPath.isEmpty()) {
            throw new MissingConfigKeyException("database.backendPath");
        }

        if (mode.equalsIgnoreCase("dev")) {
            log.info("Preparing for local development database...");
            startMongoService();
            prepareDevDatabase(backendPath);
        } else if (mode.equalsIgnoreCase("prod")) {
            log.info("Preparing for production database...");

        } else {
            throw new UnsupportedDatabaseMode(config.getDatabase().getMode());
        }
    }

    public void startMongoService() throws IOException, InterruptedException {
        log.info("Starting local MongoDB database...");
        Utils.runCommand("sudo systemctl start mongod");

        int retries = 5;
        int delayMs = 500;

        for (int i = 0; i < retries; i++) {
            Utils.CompletedProcess status = Utils.runCommand("systemctl is-active mongod");
            if (status.stdout().trim().equals("active")) {
                log.info("MongoDB service activated.");
                return;
            }
            log.info("Waiting for MongoDB service to be active... (attempt {}/{})", i + 1, retries);
            Thread.sleep(delayMs);
        }
        throw new MongoServiceNotStartedException();
    }

    public void prepareDevDatabase(String backendPath) throws IOException, InterruptedException {
        if (isScreenSessionRunning(backendPath)) {
            log.info("Dev backend already running in screen session 'sentinel-backend'. Skipping start.");
            return;
        }
        Config config = Config.getInstance();
        int backendTimeout = config.getDatabase().getBackendTimeoutSeconds();

        log.info("Starting sentinel-backend locally...");
        String startScript = backendPath + "/start-server.sh " + backendTimeout;
        String makeScriptExecutable = "chmod +X " + startScript;
        String scriptCommand = makeScriptExecutable + " && ./" + startScript;

        String completeCommand = "screen -dmS sentinel-backend bash -c \"" + scriptCommand + "\"";
        Utils.runCommand(completeCommand);
        int retries = 30;
        int delayMs = 1000;
        isServerRunning(retries, delayMs);
    }

    public void isServerRunning(int retries, int delayMs) throws IOException, InterruptedException {
        Path readyFile = Paths.get("/tmp/backend-ready");
        boolean started = false;

        for (int i = 0; i < retries; i++) {
            if (Files.exists(readyFile)) {
                String status = Files.readString(readyFile).trim();
                if (status.equals("READY")) {
                    log.info("Backend started successfully.");
                    started = true;
                }
                Files.deleteIfExists(readyFile);
                break;
            }
            log.info("Waiting for backend to be ready... (attempt {}/{})", i + 1, retries);
            Thread.sleep(delayMs);
        }
        if (!started) {
            throw new ServerNotStartedException();
        }
    }

    public boolean isScreenSessionRunning(String sessionName) {
        try {
            Utils.CompletedProcess result = Utils.runCommand("screen -ls | grep " + sessionName);
            return result.returnCode() == 0 && !result.stdout().isBlank();
        } catch (IOException | InterruptedException e) {
            log.error("Could not check screen sessions", e);
            return false;
        }
    }
}
