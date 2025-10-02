package be.unamur.snail.sentinelbackend;

import be.unamur.snail.config.Config;
import be.unamur.snail.exceptions.UnsupportedDatabaseMode;
import be.unamur.snail.utils.CommandRunner;

public class SimpleBackendServiceManagerFactoryImpl implements BackendServiceManagerFactory {
    @Override
    public BackendServiceManager create(String mode, CommandRunner runner, String serverPath) {
        Config config = Config.getInstance();

        if ("dev".equalsIgnoreCase(mode)) {
            return new DevBackendServiceManager(runner, serverPath, config.getBackend().getNbCheckServerStart(), 5000);
        } else if ("prod".equalsIgnoreCase(mode)) {
            return new ProdBackendServiceManager(runner, serverPath);
        } else {
            throw new UnsupportedDatabaseMode(mode);
        }
    }
}
