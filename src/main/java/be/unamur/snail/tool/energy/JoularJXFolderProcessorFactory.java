package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.serializer.DataSerializer;

public class JoularJXFolderProcessorFactory implements FolderProcessorFactory {
    @Override
    public FolderProcessor create(Context context) {
        Config config = Config.getInstance();
        Config.ImportConfig importConfig = config.getExecutionPlan().getEnergyMeasurements().getImportConfig();

        PipelineLogger log =  context.getLogger();

        SimpleHttpClient httpClient = new SimpleHttpClient();
        DataSerializer serializer = new DataSerializer();

        JoularJXFileProcessor fileProcessor = new JoularJXFileProcessor(serializer, httpClient, importConfig, log);
        return new JoularJXFolderProcessor(fileProcessor, log);
    }
}
