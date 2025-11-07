package be.unamur.snail.tool.energy.serializer;

import be.unamur.snail.spoon.constructor_instrumentation.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSerializer {
    private final ObjectMapper mapper;

    public DataSerializer() {
        this.mapper = new ObjectMapper();
    }

    public String serialize(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }
}
