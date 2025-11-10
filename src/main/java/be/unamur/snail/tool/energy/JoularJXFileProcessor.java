package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.MethodMeasurementDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;
import be.unamur.snail.tool.energy.serializer.DataSerializer;
import be.unamur.snail.utils.Utils;
import be.unamur.snail.utils.parser.CsvParser;
import be.unamur.snail.utils.parser.JoularJXPathParser;

import java.io.IOException;
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
            MeasurementLevel measurementLevel = JoularJXMapper.mapMeasurementLevel(pathInfo.measurementType());
            MonitoringType monitoringType = JoularJXMapper.mapMonitoringType(pathInfo.monitoringType());

            if (!isAllowed(scope, measurementLevel, monitoringType)) {
                log.debug("Skipping file {}: scope {}, measurement type {}, monitoring type {} not allowed by config", path, scope, measurementLevel, monitoringType);
                return;
            }

            log.info("Importing file: {}", path);
            CommitSimpleDTO commit = JoularJXMapper.mapCommit();

            if (monitoringType == MonitoringType.CALLTREES) {
                processCallTrees(path, scope, measurementLevel, monitoringType, iteration, commit, context);
            } else if (monitoringType == MonitoringType.METHODS) {
                processMethods(path, scope, measurementLevel, monitoringType, iteration, commit, context);
            } else {
                log.debug("Skipping file {}: unsupported monitoring type {}", path, monitoringType);
            }
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

    public void processCallTrees(Path path, Scope scope, MeasurementLevel measurementLevel, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit, Context context) throws IOException, InterruptedException {
        List<CallTreeMeasurementDTO> dtos = CsvParser.parseCallTreeFile(
                path,
                scope,
                measurementLevel,
                monitoringType,
                iteration,
                commit,
                context
        );
        log.info("Calltrees DTOs parsed from file {}: {}", path, dtos.size());
        String json = serializer.serialize(dtos);
        String url = Utils.createEndpointURL(Config.getInstance(), "/api/v2/call-tree-measurements-entities/bulk");
        httpClient.post(url, json);
    }

    public void processMethods(Path path, Scope scope, MeasurementLevel measurementLevel, MonitoringType monitoringType, RunIterationDTO iteration, CommitSimpleDTO commit, Context context) throws IOException, InterruptedException {
        List<MethodMeasurementDTO> dtos = CsvParser.parseMethodFile(
                path,
                scope,
                measurementLevel,
                monitoringType,
                iteration,
                commit,
                context
        );
        log.info("Methods DTOs parsed from file {}: {}", path, dtos.size());
        String json = serializer.serialize(dtos);
        httpClient.post("/api/v2/method-measurements-entities/bulk", json);
    }
}
