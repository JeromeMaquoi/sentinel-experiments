package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.ToolReleaseFetcher;
import be.unamur.snail.tool.ToolReleaseFetcherFactory;
import be.unamur.snail.tool.ToolReleaseResult;

/**
 * Stage responsible for the retrieve of a release of the tool used
 * for measurements
 */
public class RetrieveToolReleaseStage implements Stage {
    private final ToolReleaseFetcherFactory fetcherFactory;

    public RetrieveToolReleaseStage() {
        this.fetcherFactory = new ToolReleaseFetcherFactory();
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        Config config = Config.getInstance();
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        log.info("Retrieving release for tool {}", toolName);

        ToolReleaseFetcher fetcher = fetcherFactory.createFectcher(toolName);
        ToolReleaseResult result = fetcher.fetchRelease();
        String toolPath = result.path() + "/" + toolName + "-" + result.version() + ".jar";
        context.setEnergyToolPath(toolPath);
        context.setEnergyToolVersion(result.version());

        log.info("Retrieved release for tool {} at {}", toolName, toolPath);
    }
}
