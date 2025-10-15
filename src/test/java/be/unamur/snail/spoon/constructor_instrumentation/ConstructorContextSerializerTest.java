package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorContextSerializerTest {
    private ConstructorContextSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new ConstructorContextSerializer();
    }

    @Test
    void serializeSimpleConstructorContextWorkingTest() {
        ConstructorContext context = new ConstructorContext().withFileName("file.java").withClassName("Class").withMethodName("method").withParameters(List.of("java.lang.String")).withAttributes(new HashSet<>());

        String json = serializer.serialize(context);
        System.out.println(json);

        assertNotNull(json);
        assertEquals("""
                {
                  "fileName" : "file.java",
                  "className" : "Class",
                  "methodName" : "method",
                  "parameters" : [ "java.lang.String" ],
                  "attributes" : [ ],
                  "stackTrace" : null,
                  "snapshot" : null,
                  "empty" : false,
                  "complete" : false
                }""", json);
    }
}