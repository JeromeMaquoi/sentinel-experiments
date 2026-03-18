package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.services.ImportValidationService;
import be.unamur.snail.tool.energy.model.*;
import be.unamur.snail.tool.energy.serializer.DataSerializer;
import be.unamur.snail.utils.ChecksumUtil;
import be.unamur.snail.utils.Utils;
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

    public void processFile(Path path, RunIterationDTO iteration, Context context) {
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

            log.debug("Importing file: {}", path);
            CommitSimpleDTO commit = JoularJXMapper.mapCommit();

            // Parse CSV file with detailed error tracking
            ParseResult<? extends BaseMeasurementDTO> parseResult = CsvParser.parseCsvFileWithDetails(
                    path, scope, measurementLevel, monitoringType, iteration, commit, context
            );

            List<? extends BaseMeasurementDTO> dtos = parseResult.getParsedItems();
            
            log.debug("CSV File '{}' parse result: Total lines: {}, Parsed: {}, Errors: {}, Success rate: {:.2}%",
                    path.getFileName(),
                    parseResult.getTotalLinesRead(),
                    parseResult.getSuccessfulCount(),
                    parseResult.getErrorCount(),
                    parseResult.getSuccessRate());

            // Validate DTOs before sending to backend
            ImportValidationService validationService = new ImportValidationService(log);
            ImportValidationService.ValidationResult validationResult = validationService.validate(dtos);
            
            if (!validationResult.isValid()) {
                log.warn("Validation found {} invalid items out of {}", 
                        validationResult.getInvalidCount(), dtos.size());
                for (String error : validationResult.getErrors()) {
                    log.warn("Validation error: {}", error);
                }
            }

            List<BaseMeasurementDTO> validDtos = validationResult.getValidDtos();
            
            if (validDtos.isEmpty()) {
//                log.warn("No valid DTOs to send to backend after parsing and validation");
                return;
            }

            log.debug("Sending {} items to backend (parsed: {}, validated: {})",
                    validDtos.size(), dtos.size(), validDtos.size());

            String json = serializer.serialize(validDtos);

            // TODO put the endpoint into the configuration file
            String endpoint = String.format(
                    "/api/v2/measurements/%s/%s/bulk",
                    measurementLevel.name().toLowerCase(),
                    monitoringType.name().toLowerCase()
            );
            String url = Utils.createEndpointURL(Config.getInstance(), endpoint);

            httpClient.post(url, json);
            
            log.debug("Successfully sent {} items to backend endpoint: {}", validDtos.size(), endpoint);
            
        } catch (Exception e) {
            log.error("Error processing file {}: {}", path, e.getMessage(), e);
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
