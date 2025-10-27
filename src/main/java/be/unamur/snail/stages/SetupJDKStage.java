package be.unamur.snail.stages;

import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.jdk.JdkVersionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

/**
 * Stage responsible for detecting, installing and configuring the correct
 * JDK version for the analyzed project.
 */
public class SetupJDKStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(SetupJDKStage.class);

    private final JdkVersionDetector detector;

    public SetupJDKStage(JdkVersionDetector detector) {
        this.detector = detector;
    }

    @Override
    public void execute(Context context) throws Exception {
        if (context.getRepoPath() == null) {
            throw new MissingContextKeyException("repoPath");
        }

        File repoPath = new File(context.getRepoPath());
        log.info("Starting JDK setup stage for {}", repoPath);

        Optional<String> detectedVersion = detector.detectVersion(repoPath);
        String jdkVersion = detectedVersion.orElseGet(() -> {
            log.warn("No JDK version detected, falling back to default JDK (system)");
            return System.getProperty("java.version");
        });

        log.info("Using JDK version: {}", jdkVersion);

        // Here you would add the logic to install and configure the JDK version
        // For example, using SDKMAN!, jEnv, or any other JDK version manager

    }
}
