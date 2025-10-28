package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.JavaHomeNotFoundException;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.jdk.JdkManager;
import be.unamur.snail.jdk.SdkmanJdkManager;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stage responsible for detecting, installing and configuring the correct
 * JDK version for the analyzed project.
 */
public class SetupJdkStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(SetupJdkStage.class);

    private final JdkManager manager;
    private final Config config;
    private final CommandRunner runner;

    public SetupJdkStage() {
        this(new SdkmanJdkManager(new SimpleCommandRunner()), Config.getInstance(), new SimpleCommandRunner());
    }

    public SetupJdkStage(JdkManager manager, Config config, CommandRunner runner) {
        this.manager = manager;
        this.config = config;
        this.runner = runner;
    }

    @Override
    public void execute(Context context) throws Exception {
        String jdkVersion = config.getRepo().getJdk();
        if (jdkVersion == null || jdkVersion.isBlank()) {
            throw new MissingConfigKeyException("repo.jdk");
        }

        log.info("Setting up JDK version {}", jdkVersion);

        if (!manager.isInstalled(jdkVersion)) {
            log.info("JDK version {} is not installed. Installing...", jdkVersion);
            manager.install(jdkVersion);
        }
        manager.use(jdkVersion);

        String javaHome = manager.getJavaHome(jdkVersion);
        if (javaHome == null || javaHome.isBlank()) {
            throw new JavaHomeNotFoundException(jdkVersion);
        }
        context.setJavaHome(javaHome);
        log.info("JAVA_HOME set to {}", javaHome);

        // Export JAVA_HOME in the shell for subsequent commands
        String exportCommand = "export JAVA_HOME=" + javaHome;
        runner.run(exportCommand);
    }
}
