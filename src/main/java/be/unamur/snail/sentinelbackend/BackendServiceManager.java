package be.unamur.snail.sentinelbackend;

import java.io.IOException;

public interface BackendServiceManager {
    boolean startBackend() throws IOException, InterruptedException;
    boolean stopBackend() throws IOException, InterruptedException;
}
