package be.unamur.snail.spoon.constructor_instrumentation;

import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RepositorySimpleDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorContextTest {
    @Test
    void isCompleteShouldReturnTrueIfAllNecessaryFieldsAreSetTest() {
        RepositorySimpleDTO repo = new RepositorySimpleDTO("project", "owner");
        CommitSimpleDTO commit = new CommitSimpleDTO("sha", repo);
        ConstructorContext context = new ConstructorContext().withFileName("name").withClassName("class").withMethodName("method").withParameters(List.of()).withStackTrace(List.of()).withAttributes(new ArrayList<>()).withCommit(commit);
        AttributeContext attribute = new AttributeContext("name", "type", "actualType", "rhs");
        context.addAttribute(attribute);

        assertTrue(context.isComplete());
    }

    @Test
    void isCompleteShouldReturnFalseIfAllNecessaryFieldsAreNotSetTest() {
        ConstructorContext context = new ConstructorContext().withFileName("name").withClassName("class").withMethodName("method").withParameters(List.of()).withAttributes(new ArrayList<>());
        AttributeContext attribute = new AttributeContext("name", "type", "actualType", "rhs");
        context.addAttribute(attribute);
        System.out.println(context);

        assertFalse(context.isComplete());
    }

    @Test
    void copyCreatesEqualButIndependentObjectTest() {
        List<String> params = new ArrayList<>();
        params.add("param1");
        List<AttributeContext> attributes = new ArrayList<>();
        attributes.add(new AttributeContext("name", "type", "actual", "rhs"));
        List<StackTraceElement> stacktrace = List.of(new StackTraceElement("Class", "method", "file.java", 10));

        ConstructorContext original = new ConstructorContext()
                .withFileName("file.java")
                .withClassName("MyClass")
                .withMethodName("myMethod")
                .withParameters(params)
                .withAttributes(attributes)
                .withStackTrace(stacktrace)
                .withSnapshot("snapshot1");

        ConstructorContext copy = original.copy();

        assertEquals(original, copy);
        assertNotSame(original, copy);

        assertNotSame(original.getParameters(), copy.getParameters());
        assertNotSame(original.getAttributes(), copy.getAttributes());
        assertNotSame(original.getStacktrace(), copy.getStacktrace());

        copy.getParameters().add("param2");
        copy.getAttributes().clear();
        copy.getStacktrace().clear();
        copy.withSnapshot("snapshot2");

        assertEquals(1, original.getParameters().size());
        assertEquals(1, original.getAttributes().size());
        assertEquals(1, original.getStacktrace().size());
        assertEquals("snapshot1", original.getSnapshot());
    }
}