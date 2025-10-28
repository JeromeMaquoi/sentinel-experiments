package be.unamur.snail.jdk;

import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class SdkmanJdkManager implements JdkManager {
    private static final Logger log = LoggerFactory.getLogger(SdkmanJdkManager.class);
    private final CommandRunner runner;
    private final String sdkmanInit;

    public SdkmanJdkManager(CommandRunner runner) {
        this(runner, System.getProperty("user.home") + "/.sdkman/bin/sdkman-init.sh");
    }

    public SdkmanJdkManager(CommandRunner runner, String sdkmanInit) {
        this.runner = runner;
        this.sdkmanInit = sdkmanInit;
    }

    private String withSdkmanInit(String command) {
        return "source " + sdkmanInit + " && " + command;
    }

    @Override
    public boolean isInstalled(String version) throws IOException, InterruptedException {
        Utils.CompletedProcess result = runner.run(withSdkmanInit("sdk list java"));
        if (result == null) return false;
        String out = Optional.ofNullable(result.stdout()).orElse("");
        boolean found = Arrays.stream(out.split("\\R"))
                .anyMatch(line -> line.contains(version) && line.contains("installed"));
        log.info("sdk list java contains '{}' ? -> {}", version, found);
        return found;
    }

    @Override
    public void install(String version) throws IOException, InterruptedException {
        log.info("Installing JDK version {} via SDKMAN!", version);
        String command = "echo 'n' | sdk install java " + version;
        Utils.CompletedProcess result = runner.run(withSdkmanInit(command));
        if (result.returnCode() != 0) {
            throw new IOException("Failed to install JDK version " + version + " via SDKMAN!: " + result.stderr());
        }
    }

    @Override
    public void use(String version) throws IOException, InterruptedException {
        log.info("Switching to JDK version {} via SDKMAN!", version);
        Utils.CompletedProcess result = runner.run(withSdkmanInit("sdk use java " + version));
        if (result.returnCode() != 0) {
            throw new IOException("Failed to switch to JDK version " + version + " via SDK");
        }
    }

    @Override
    public String getJavaHome(String version) throws IOException, InterruptedException {
        Utils.CompletedProcess result = runner.run(withSdkmanInit("sdk home java " + version));
        String out = Optional.ofNullable(result.stdout()).orElse("").trim();
        if (!out.isBlank()) {
            String firstLine = out.split("\\R")[0].trim();
            if (firstLine.startsWith("/")) {
                log.debug("sdk home returned {}", firstLine);
                return firstLine;
            }
        }
        String fallback = System.getProperty("user.home") + "/.sdkman/candidates/java/" + version;
        log.warn("Could not determine JAVA_HOME via 'sdk home', falling back to {}", fallback
        );
        return fallback;
    }
}
