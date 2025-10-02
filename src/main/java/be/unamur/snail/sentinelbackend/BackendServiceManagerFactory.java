package be.unamur.snail.sentinelbackend;

import be.unamur.snail.utils.CommandRunner;

public interface BackendServiceManagerFactory {
    BackendServiceManager create(String mode, CommandRunner runner, String serverPath);
}
