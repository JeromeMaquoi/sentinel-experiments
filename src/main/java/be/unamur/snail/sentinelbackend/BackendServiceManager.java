package be.unamur.snail.sentinelbackend;

import java.io.IOException;

public interface BackendServiceManager {
    public boolean startBackend() throws IOException, InterruptedException;
}
