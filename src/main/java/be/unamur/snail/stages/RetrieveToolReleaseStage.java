package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.tool.ToolReleaseFetcher;
import be.unamur.snail.tool.ToolReleaseFetcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveToolReleaseStage implements Stage {
    private static final Logger log = LoggerFactory.getLogger(RetrieveToolReleaseStage.class);
    private final ToolReleaseFetcherFactory fetcherFactory;

    public RetrieveToolReleaseStage() {
        this.fetcherFactory = new ToolReleaseFetcherFactory();
    }

    @Override
    public void execute(Context context) throws Exception {
        Config config = Config.getInstance();
        String toolName = config.getExecutionPlan().getEnergyMeasurements().getTool();
        log.info("Retrieving release for tool {}", toolName);

        ToolReleaseFetcher fetcher = fetcherFactory.createFectcher(toolName);
        String toolPath = fetcher.fetchRelease(context);

        log.info("Retrieved release for tool {}", toolPath);
    }
}
