package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.*;
import be.unamur.snail.tool.energy.serializer.DataSerializer;
import be.unamur.snail.utils.parser.CsvParser;
import be.unamur.snail.utils.parser.JoularJXPathParser;

import java.nio.file.Path;
import java.util.List;

public class JoularJXFileProcessor {
    private final DataSerializer serializer;
    private final SimpleHttpClient httpClient;
    private final Config.ImportConfig importConfig;
    private final PipelineLogger log;

    public JoularJXFileProcessor(DataSerializer serializer, SimpleHttpClient httpClient, Config.ImportConfig importConfig, PipelineLogger log) {
        this.serializer = serializer;
        this.httpClient = httpClient;
        this.importConfig = importConfig;
        this.log = log;
    }

    public void process(Path path, RunIterationDTO iteration, Context context) {
        try {
            JoularJXPathParser.PathInfo pathInfo = parsePath(path);
            if (pathInfo == null) return;

            Scope scope = JoularJXMapper.mapScope(pathInfo.scope());
            MeasurementLevel measurementLevel = JoularJXMapper.mapMeasurementLevel(pathInfo.measurementLevel());
            MonitoringType monitoringType = JoularJXMapper.mapMonitoringType(pathInfo.monitoringType());

            if (!isAllowed(scope, measurementLevel, monitoringType)) {
                log.debug("Skipping file {}: scope {}, measurement type {}, monitoring type {} not allowed by config", path, scope, measurementLevel, monitoringType);
                return;
            }

            log.info("Importing file: {}", path);
            CommitSimpleDTO commit = JoularJXMapper.mapCommit();

            List<? extends BaseMeasurementDTO> dtos = CsvParser.parseCsvFile(path, scope, measurementLevel, monitoringType, iteration, commit, context);
            String json = serializer.serialize(dtos);
            String endpoint = String.format(
                    "/api/v2/measurements/%s/%s/bulk",
                    measurementLevel.name().toLowerCase(),
                    monitoringType.name().toLowerCase()
            );
            httpClient.post(endpoint, json);
        } catch (Exception e) {
            log.error("Error processing file {}: {}", path, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public JoularJXPathParser.PathInfo parsePath(Path path) {
        try {
            return JoularJXPathParser.parse(path);
        } catch (IllegalArgumentException e) {
            log.debug("Skipping file {}: {}", path, e.getMessage());
            return null;
        }
    }

    public boolean isAllowed(Scope scope, MeasurementLevel measurementLevel, MonitoringType monitoringType) {
        return importConfig.getScopes().contains(scope.toString()) &&
               importConfig.getMeasurementTypes().contains(measurementLevel.toString()) &&
               importConfig.getMonitoringTypes().contains(monitoringType.toString());
    }
}
