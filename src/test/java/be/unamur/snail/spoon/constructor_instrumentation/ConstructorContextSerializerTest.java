package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

        //Arrange
        ConstructorContext context = new ConstructorContext()
                .withFileName("file.java")
                .withClassName("Class")
                .withMethodName("method")
                .withParameters(List.of("java.lang.String"))
                .withAttributes(new ArrayList<>());

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"file.java\"," +
                        "\"className\":\"Class\"," +
                        "\"methodName\":\"method\"," +
                        "\"parameters\":[\"java.lang.String\"]," +
                        "\"attributes\":[]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithNoParameters() {
        //Arrange
        ConstructorContext context = new ConstructorContext()
                .withFileName("Service.java")
                .withClassName("UserService")
                .withMethodName("UserService")
                .withParameters(new ArrayList<>())
                .withAttributes(new ArrayList<>());

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Service.java\"," +
                        "\"className\":\"UserService\"," +
                        "\"methodName\":\"UserService\"," +
                        "\"parameters\":[]," +
                        "\"attributes\":[]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithMultipleParameters() {

        //Arrange
        ConstructorContext context = new ConstructorContext()
                .withFileName("Repository.java")
                .withClassName("OrderRepository")
                .withMethodName("OrderRepository")
                .withParameters(List.of("java.lang.String", "int", "boolean"))
                .withAttributes(new ArrayList<>());

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Repository.java\"," +
                        "\"className\":\"OrderRepository\"," +
                        "\"methodName\":\"OrderRepository\"," +
                        "\"parameters\":[\"java.lang.String\",\"int\",\"boolean\"]," +
                        "\"attributes\":[]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithOneAttribute() {

        //Arrange
        AttributeContext attr = new AttributeContext("name", "String", "java.lang.String", "\"Alice\"");

        ConstructorContext context = new ConstructorContext()
                .withFileName("Person.java")
                .withClassName("Person")
                .withMethodName("Person")
                .withParameters(List.of("java.lang.String"))
                .withAttributes(List.of(attr));

        //Act
        String json = serializer.serialize(context);

        //Asssert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Person.java\"," +
                        "\"className\":\"Person\"," +
                        "\"methodName\":\"Person\"," +
                        "\"parameters\":[\"java.lang.String\"]," +
                        "\"attributes\":[{\"name\":\"name\",\"type\":\"String\",\"actualType\":\"java.lang.String\",\"rhs\":\"\\\"Alice\\\"\"}]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithMultipleAttributes() {

        //Arrange
        AttributeContext attr1 = new AttributeContext("id", "int", "int", "42");
        AttributeContext attr2 = new AttributeContext("label", "String", "java.lang.String", "null");

        ConstructorContext context = new ConstructorContext()
                .withFileName("Item.java")
                .withClassName("Item")
                .withMethodName("Item")
                .withParameters(List.of("int", "java.lang.String"))
                .withAttributes(List.of(attr1, attr2));

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Item.java\"," +
                        "\"className\":\"Item\"," +
                        "\"methodName\":\"Item\"," +
                        "\"parameters\":[\"int\",\"java.lang.String\"]," +
                        "\"attributes\":[" +
                        "{\"name\":\"id\",\"type\":\"int\",\"actualType\":\"int\",\"rhs\":\"42\"}," +
                        "{\"name\":\"label\",\"type\":\"String\",\"actualType\":\"java.lang.String\",\"rhs\":\"null\"}" +
                        "]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }
    @Test
    void serializeContextWithStacktrace() {
        //Arrange
        StackTraceElement frame = new StackTraceElement(
                "com.example.Main", "main", "Main.java", 10);

        ConstructorContext context = new ConstructorContext()
                .withFileName("Main.java")
                .withClassName("Main")
                .withMethodName("Main")
                .withParameters(new ArrayList<>())
                .withAttributes(new ArrayList<>())
                .withStackTrace(List.of(frame));

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Main.java\"," +
                        "\"className\":\"Main\"," +
                        "\"methodName\":\"Main\"," +
                        "\"parameters\":[]," +
                        "\"attributes\":[]," +
                        "\"stacktrace\":[{\"className\":\"com.example.Main\",\"methodName\":\"main\",\"fileName\":\"Main.java\",\"lineNumber\":10}]," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithSnapshot() {

        //Arrange
        ConstructorContext context = new ConstructorContext()
                .withFileName("Snapshot.java")
                .withClassName("Snapshot")
                .withMethodName("Snapshot")
                .withParameters(new ArrayList<>())
                .withAttributes(new ArrayList<>())
                .withSnapshot("some-snapshot-content");

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":\"Snapshot.java\"," +
                        "\"className\":\"Snapshot\"," +
                        "\"methodName\":\"Snapshot\"," +
                        "\"parameters\":[]," +
                        "\"attributes\":[]," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":\"some-snapshot-content\"," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeContextWithAllNullFields() {

        //Arrange
        ConstructorContext context = new ConstructorContext();

        //Act
        String json = serializer.serialize(context);

        //Assert
        assertNotNull(json);
        assertEquals(
                "{\"fileName\":null," +
                        "\"className\":null," +
                        "\"methodName\":null," +
                        "\"parameters\":null," +
                        "\"attributes\":null," +
                        "\"stacktrace\":null," +
                        "\"snapshot\":null," +
                        "\"commit\":null}",
                json);
    }

    @Test
    void serializeListWithTwoContexts() {

        //Arrange
        ConstructorContext c1 = new ConstructorContext()
                .withFileName("A.java")
                .withClassName("A")
                .withMethodName("A")
                .withParameters(new ArrayList<>())
                .withAttributes(new ArrayList<>());

        ConstructorContext c2 = new ConstructorContext()
                .withFileName("B.java")
                .withClassName("B")
                .withMethodName("B")
                .withParameters(List.of("int"))
                .withAttributes(new ArrayList<>());

        //Act
        String json = serializer.serializeList(List.of(c1, c2));

        //Assert
        assertNotNull(json);
        assertEquals(
                "[" +
                        "{\"fileName\":\"A.java\",\"className\":\"A\",\"methodName\":\"A\",\"parameters\":[],\"attributes\":[],\"stacktrace\":null,\"snapshot\":null,\"commit\":null}," +
                        "{\"fileName\":\"B.java\",\"className\":\"B\",\"methodName\":\"B\",\"parameters\":[\"int\"],\"attributes\":[],\"stacktrace\":null,\"snapshot\":null,\"commit\":null}" +
                        "]",
                json);
    }

    @Test
    void serializeListEmpty() {
        //Act
        String json = serializer.serializeList(new ArrayList<>());

        //Assert
        assertNotNull(json);
        assertEquals("[]", json);
    }
}
