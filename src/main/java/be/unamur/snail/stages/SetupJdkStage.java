package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.JavaHomeNotFoundException;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.jdk.JdkManager;
import be.unamur.snail.jdk.SdkmanJdkManager;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.utils.CommandRunner;
import be.unamur.snail.utils.SimpleCommandRunner;
import be.unamur.snail.utils.Utils;

import java.io.IOException;
import java.util.Optional;

/**
 * Stage responsible for detecting, installing and configuring the correct
 * JDK version for the analyzed project.
 */
public class SetupJdkStage implements Stage {
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
        PipelineLogger log = context.getLogger();

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
        runner.run("export JAVA_HOME=" + javaHome);
        updateAlternatives(log, javaHome);
    }

    /**
     * Ensures that update-alternatives recognizes and uses the JDK's java binary.
     * This method is idempotent and safe to call repeatedly.
     * @param log The pipeline logger
     * @param javaHome The JAVA_HOME path of the JDK
     */
    public void updateAlternatives(PipelineLogger log, String javaHome) throws IOException, InterruptedException {
        String javaBinary = javaHome + "/bin/java";

        Utils.CompletedProcess check = runner.run("update-alternatives --query java");
        boolean alreadyRegistered = Optional.ofNullable(check.stdout())
                .map(out -> out.contains(javaBinary))
                .orElse(false);
        if (!alreadyRegistered) {
            log.info("Registering {} as an alterative for java", javaBinary);
            Utils.CompletedProcess install = runner.run(
                    "sudo update-alternatives --install /usr/bin/java java " + javaBinary + " 1"
            );
            if (install.returnCode() != 0) {
                throw new IOException("Failed to register " + javaBinary + " as an alternative for java: " + install.stderr());
            }
        } else {
            log.info("{} is already registered as an alternative for java", javaBinary);
        }

        log.info("Setting {} as the active java alternative", javaBinary);
        Utils.CompletedProcess set = runner.run(
                "sudo update-alternatives --set java " + javaBinary
        );
        if (set.returnCode() != 0) {
            throw new IOException("Failed to set " + javaBinary + " as the active java alternative: " + set.stderr());
        }
    }
}
