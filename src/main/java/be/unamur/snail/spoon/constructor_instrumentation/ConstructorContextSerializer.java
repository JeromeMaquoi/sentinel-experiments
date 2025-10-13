package be.unamur.snail.spoon.constructor_instrumentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ConstructorContextSerializer {
    private final ObjectMapper mapper;

    public ConstructorContextSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serialize a ConstructorContext into a JSON string
     * @param context the ConstructorContext to serialize
     * @return A JSON string containing the serialization of the context
     */
    public String serialize(ConstructorContext context) {
        try {
            return mapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }
}
