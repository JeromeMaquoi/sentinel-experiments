package be.unamur.snail.sentinelbackend;

import be.unamur.snail.utils.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProdBackendServiceManager implements BackendServiceManager {
    private static final Logger log = LoggerFactory.getLogger(ProdBackendServiceManager.class);

    private final CommandRunner runner;
    private final String backendPath;

    public ProdBackendServiceManager(CommandRunner runner, String backendPath) {
        this.runner = runner;
        this.backendPath = backendPath;
    }

    @Override
    public boolean startBackend() throws IOException, InterruptedException {
        // TODO
        return false;
    }
}
