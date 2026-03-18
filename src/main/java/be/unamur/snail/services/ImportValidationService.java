package be.unamur.snail.services;

import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.tool.energy.model.BaseMeasurementDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to validate measurement DTOs before sending them to the backend.
 * Ensures data integrity and completeness.
 */
public class ImportValidationService {
    private final PipelineLogger log;

    public ImportValidationService(PipelineLogger log) {
        this.log = log;
    }

    /**
     * Validate a list of measurement DTOs before backend submission.
     *
     * @param dtos The DTOs to validate
     * @return ValidationResult containing valid DTOs and any validation errors
     */
    public ValidationResult validate(List<? extends BaseMeasurementDTO> dtos) {
        List<BaseMeasurementDTO> validDtos = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            BaseMeasurementDTO dto = dtos.get(i);
            List<String> itemErrors = validateDTO(dto, i);
            
            if (itemErrors.isEmpty()) {
                validDtos.add(dto);
            } else {
                validationErrors.addAll(itemErrors);
            }
        }

        return new ValidationResult(validDtos, validationErrors);
    }

    /**
     * Validate a single DTO and return list of validation errors (empty if valid).
     */
    private List<String> validateDTO(BaseMeasurementDTO dto, int index) {
        List<String> errors = new ArrayList<>();

        // Check for null fields
        if (dto.getScope() == null) {
            errors.add(String.format("Item %d: Missing scope", index));
        }
        if (dto.getMeasurementLevel() == null) {
            errors.add(String.format("Item %d: Missing measurement level", index));
        }
        if (dto.getMonitoringType() == null) {
            errors.add(String.format("Item %d: Missing monitoring type", index));
        }
        if (dto.getValue() == null || Float.isNaN(dto.getValue())) {
            errors.add(String.format("Item %d: Invalid or missing value", index));
        }
        if (dto.getIteration() == null) {
            errors.add(String.format("Item %d: Missing iteration information", index));
        }

        // Check for value ranges
        if (dto.getValue() != null && dto.getValue() < 0) {
            errors.add(String.format("Item %d: Negative energy value (%.4f)", index, dto.getValue()));
        }

        return errors;
    }

    /**
     * Result of validation containing valid DTOs and any errors found.
     */
    public static class ValidationResult {
        private final List<BaseMeasurementDTO> validDtos;
        private final List<String> errors;

        public ValidationResult(List<BaseMeasurementDTO> validDtos, List<String> errors) {
            this.validDtos = validDtos;
            this.errors = errors;
        }

        public List<BaseMeasurementDTO> getValidDtos() {
            return validDtos;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public int getValidCount() {
            return validDtos.size();
        }

        public int getInvalidCount() {
            return errors.size();
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%d, invalid=%d}",
                    getValidCount(), getInvalidCount());
        }
    }
}

