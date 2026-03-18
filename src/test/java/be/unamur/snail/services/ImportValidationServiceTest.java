package be.unamur.snail.services;

import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.MeasurementLevel;
import be.unamur.snail.tool.energy.MonitoringType;
import be.unamur.snail.tool.energy.Scope;
import be.unamur.snail.tool.energy.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for ImportValidationService.
 */
class ImportValidationServiceTest {
    @Mock
    private PipelineLogger log;

    private ImportValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validationService = new ImportValidationService(log);
    }

    @Test
    void validateShouldPassValidDTOsTest() {
        List<BaseMeasurementDTO> dtos = new ArrayList<>();
        TotalMethodMeasurementDTO dto = new TotalMethodMeasurementDTO();
        dto.setMethod("test.method");
        dto.setScope(Scope.APP);
        dto.setMeasurementLevel(MeasurementLevel.TOTAL);
        dto.setMonitoringType(MonitoringType.METHODS);
        dto.setValue(10.5f);
        dto.setIteration(new RunIterationDTO());
        dtos.add(dto);

        ImportValidationService.ValidationResult result = validationService.validate(dtos);

        assertTrue(result.isValid());
        assertEquals(1, result.getValidCount());
        assertEquals(0, result.getInvalidCount());
    }

    @Test
    void validateShouldRejectDTOsWithMissingFieldsTest() {
        List<BaseMeasurementDTO> dtos = new ArrayList<>();
        TotalMethodMeasurementDTO dto = new TotalMethodMeasurementDTO();
        dto.setMethod("test.method");
        // Missing scope and other required fields
        dtos.add(dto);

        ImportValidationService.ValidationResult result = validationService.validate(dtos);

        assertFalse(result.isValid());
        assertTrue(result.getInvalidCount() > 0);
    }

    @Test
    void validateShouldRejectDTOsWithNegativeValuesTest() {
        List<BaseMeasurementDTO> dtos = new ArrayList<>();
        TotalMethodMeasurementDTO dto = new TotalMethodMeasurementDTO();
        dto.setMethod("test.method");
        dto.setScope(Scope.APP);
        dto.setMeasurementLevel(MeasurementLevel.TOTAL);
        dto.setMonitoringType(MonitoringType.METHODS);
        dto.setValue(-10.5f); // Negative value
        dto.setIteration(new RunIterationDTO());
        dtos.add(dto);

        ImportValidationService.ValidationResult result = validationService.validate(dtos);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Negative")));
    }

    @Test
    void validateShouldMixValidAndInvalidDTOsTest() {
        List<BaseMeasurementDTO> dtos = new ArrayList<>();

        // Valid DTO
        TotalMethodMeasurementDTO dto1 = new TotalMethodMeasurementDTO();
        dto1.setMethod("test.method");
        dto1.setScope(Scope.APP);
        dto1.setMeasurementLevel(MeasurementLevel.TOTAL);
        dto1.setMonitoringType(MonitoringType.METHODS);
        dto1.setValue(10.5f);
        dto1.setIteration(new RunIterationDTO());
        dtos.add(dto1);

        // Invalid DTO (missing value)
        TotalMethodMeasurementDTO dto2 = new TotalMethodMeasurementDTO();
        dto2.setMethod("test.method2");
        dto2.setScope(Scope.APP);
        dto2.setMeasurementLevel(MeasurementLevel.TOTAL);
        dto2.setMonitoringType(MonitoringType.METHODS);
        // Missing value
        dto2.setIteration(new RunIterationDTO());
        dtos.add(dto2);

        ImportValidationService.ValidationResult result = validationService.validate(dtos);

        assertFalse(result.isValid());
        assertEquals(1, result.getValidCount());
        assertEquals(1, result.getInvalidCount());
    }
}

