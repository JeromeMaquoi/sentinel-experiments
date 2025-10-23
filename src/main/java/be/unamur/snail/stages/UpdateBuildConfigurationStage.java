package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.UnknownProjectBuildException;
import be.unamur.snail.utils.MavenPomModifier;
import be.unamur.snail.utils.ProjectTypeDetector;
import be.unamur.snail.utils.Utils;
import be.unamur.snail.utils.gradle.InitScriptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Stage responsible for updating the pom.xml or gralde.build file to add JoularJX
 * as java agent during the test execution phase of the analyzed project
 */
public class UpdateBuildConfigurationStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(UpdateBuildConfigurationStage.class);

    private final InitScriptGenerator initScriptGenerator;
    private final Config config;
    private final ProjectTypeDetector projectTypeDetector;

    public UpdateBuildConfigurationStage() {
        this(new InitScriptGenerator(), Config.getInstance(), new ProjectTypeDetector());
    }

    public UpdateBuildConfigurationStage(InitScriptGenerator initScriptGenerator, Config config, ProjectTypeDetector projectTypeDetector) {
        this.initScriptGenerator = initScriptGenerator;
        this.config = config;
        this.projectTypeDetector = projectTypeDetector;
    }

    @Override
    public void execute(Context context) throws Exception {
        if (context.getRepoPath() == null) {
            throw new MissingContextKeyException("repoPath");
        }
        File repoPath = new File(context.getRepoPath());

        if (config.getExecutionPlan().getEnergyMeasurements().getToolPath() == null) {
            throw new MissingConfigKeyException("toolPath");
        }
        String energyToolPath = config.getExecutionPlan().getEnergyMeasurements().getToolPath();
        File initScript;
        if (projectTypeDetector.isGradleProject(repoPath)) {
            initScript = initScriptGenerator.generateGradleJavaAgentAndIterationIdInitScript(energyToolPath);
            context.setInitScript(initScript);
            log.info("Gradle init script created at {}", initScript.getAbsolutePath());
        } else if (projectTypeDetector.isMavenProject(repoPath)) {
            File pomFile = new File(repoPath, "pom.xml");
            File backupPom = MavenPomModifier.injectJavaAgent(pomFile, energyToolPath);
            context.setBackupPom(backupPom);
            log.info("Maven pom.xml updated with javaagent: {}", pomFile.getAbsolutePath());
        } else {
            throw new UnknownProjectBuildException();
        }
    }
}
