package be.unamur.snail.tool.energy.serializer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataSerializerTest {
    private final DataSerializer serializer = new DataSerializer();

    @Test
    void serializeSimpleObjectShouldWorkTest() {
        Map<String, Object> data = Map.of(
                "key1", "value1",
                "key2", 42,
                "key3", true
        );
        String json = serializer.serialize(data);

        assertNotNull(json);
        assertTrue(json.contains("\"key1\":\"value1\""));
        assertTrue(json.contains("\"key2\":42"));
        assertTrue(json.contains("\"key3\":true"));
    }

    @Test
    void serializeListShoudWorkTest() {
        List<String> list = List.of("item1", "item2", "item3");
        String json = serializer.serialize(list);

        assertNotNull(json);
        assertEquals("[\"item1\",\"item2\",\"item3\"]", json);
    }

    @Test
    void serializeInvalidObjectShouldThrowExceptionTest() {
        Object invalidObject = new Object() {
            private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
                throw new java.io.IOException("Cannot serialize");
            }
        };

        assertThrows(RuntimeException.class, () -> {
            serializer.serialize(invalidObject);
        });
    }
}