package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingContextKeyException;
import be.unamur.snail.exceptions.UnknownProjectBuildException;
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

    public UpdateBuildConfigurationStage() {
        this(new InitScriptGenerator());
    }

    public UpdateBuildConfigurationStage(InitScriptGenerator initScriptGenerator) {
        this.initScriptGenerator = initScriptGenerator;
    }

    @Override
    public void execute(Context context) throws Exception {
        File repoPath = new File(context.getRepoPath());
        if (!repoPath.exists() || !repoPath.isDirectory()) {
            throw new MissingContextKeyException("repoPath");
        }

        Config config = Config.getInstance();
        String energyToolPath = config.getExecutionPlan().getEnergyMeasurements().getToolPath();
        File initScript;
        if (Utils.isGradleProject(repoPath)) {
            initScript = initScriptGenerator.generateGradleJavaAgentAndIterationIdInitScript(energyToolPath);
            log.info("Gradle init script created at {}", initScript.getAbsolutePath());
        } else if (Utils.isMavenProject(repoPath)) {
            initScript = initScriptGenerator.createMavenArgLineFile(repoPath);
            log.info("Maven init script created at {}", initScript.getAbsolutePath());
        } else {
            throw new UnknownProjectBuildException();
        }
        context.setInitScript(initScript);
    }
}
