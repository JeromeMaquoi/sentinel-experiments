package be.unamur.snail.jdk;

import java.io.IOException;

public interface JdkManager {
    boolean isInstalled(String version) throws IOException, InterruptedException;

    void install(String version) throws IOException, InterruptedException;

    void use(String version) throws IOException, InterruptedException;

    String getJavaHome(String version) throws IOException, InterruptedException;
}
