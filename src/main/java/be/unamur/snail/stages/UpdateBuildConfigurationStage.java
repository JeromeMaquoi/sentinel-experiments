package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.UnknownProjectBuildException;
import be.unamur.snail.utils.MavenPomModifier;
import be.unamur.snail.utils.ProjectTypeDetector;
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
        if (context.getEnergyToolPath() == null) {
            throw new MissingContextKeyException("energyToolPath");
        }
        File repoPath = new File(context.getRepoPath());
        log.info("Repo path: {}", repoPath.getAbsolutePath());

        String energyToolPath = context.getEnergyToolPath();
        log.info("Energy Tool path: {}", energyToolPath);
        File initScript;
        if (projectTypeDetector.isGradleProject(repoPath)) {
            initScript = initScriptGenerator.generateGradleJavaAgentAndIterationIdInitScript(energyToolPath);
            context.setInitScript(initScript);
            log.info("Gradle init script created at {}", initScript.getAbsolutePath());
        } else if (projectTypeDetector.isMavenProject(repoPath)) {
            String mavenAgentArg = "-DargLine=-javaagent:" + energyToolPath;
            context.setMavenAgentArg(mavenAgentArg);
            log.info("Maven agent arg created: {}", mavenAgentArg);
        } else {
            throw new UnknownProjectBuildException();
        }
    }
}
