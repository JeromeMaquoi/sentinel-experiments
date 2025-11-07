package be.unamur.snail.database;

import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MongoServiceManager {
    private static final Logger log = LoggerFactory.getLogger(MongoServiceManager.class);

    private final CommandRunner runner;
    private final int nbRetries;
    private final int delayMs;

    public MongoServiceManager(CommandRunner runner, int nbRetries, int delayMs) {
        this.runner = runner;
        this.nbRetries = nbRetries;
        this.delayMs = delayMs;
    }

    public boolean startMongoService() throws IOException, InterruptedException {
        log.info("Starting MongoDB service...");
        runner.run("sudo systemctl start mongod");

        for (int i = 0; i < nbRetries; i++) {
            Utils.CompletedProcess status = runner.run("systemctl is-active mongod");
            if (status.stdout().trim().equals("active")) return true;
            Thread.sleep(delayMs);
        }
        return false;
    }

    public boolean stopMongoService() throws IOException, InterruptedException {
        log.info("Stopping MongoDB service...");
        runner.run("sudo systemctl stop mongod");

        for (int i = 0; i < nbRetries; i++) {
            Utils.CompletedProcess status = runner.run("systemctl is-active mongod");
            if (status.stdout().trim().equals("inactive")) return true;
            Thread.sleep(delayMs);
        }
        return false;
    }
}
