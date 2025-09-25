package be.unamur.snail.stages;

import be.unamur.snail.config.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.core.Stage;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MongoServiceNotStartedException;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

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
        if (!isMongoRunningLocally()) {
            throw new MongoServiceNotStartedException();
        }
        log.info("Local MongoDB database started.");
    }

    public void prepareDevDatabase(String backendPath) throws Exception {
        if (isScreenSessionRunning(backendPath)) {
            log.info("Dev backend already running in screen session 'sentinel-backend'. Skipping start.");
            return;
        }
        log.info("Starting sentinel-backend locally...");
        String command = String.join(" && ",
                "cd " + backendPath,
                "./mvnw clean",
                "./mvnw");
        Utils.runCommand("screen -dmS sentinel-backend bash -c '" + command + "'", backendPath);
        log.info("Started dev backend in detached screen session 'sentinel-backend'.");
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

    public boolean isMongoRunningLocally() {
        try (Socket socket = new Socket("localhost", 27017)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
