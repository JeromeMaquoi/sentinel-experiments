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
    private final Config config;

    public RetrieveToolReleaseStage() {
        this(new ToolReleaseFetcherFactory(), Config.getInstance());
    }

    RetrieveToolReleaseStage(ToolReleaseFetcherFactory fetcherFactory, Config config) {
        this.fetcherFactory = fetcherFactory;
        this.config = config;
    }

    @Override
    public void execute(Context context) throws Exception {
        PipelineLogger log = context.getLogger();

        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        log.debug("Retrieving release for tool {}", toolName);

        ToolReleaseFetcher fetcher = fetcherFactory.createFectcher(toolName);
        ToolReleaseResult result = fetcher.fetchRelease();
        String toolPath = result.path() + "/" + toolName + "-" + result.version() + ".jar";
        context.setEnergyToolPath(toolPath);

        log.info("Retrieved release for tool {} at {}", toolName, toolPath);
    }
}
