package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorContextTest {
    @Test
    void isCompleteShouldReturnTrueIfAllNecessaryFieldsAreSetTest() {
        ConstructorContext context = new ConstructorContext().withFileName("name").withClassName("class").withMethodName("method").withParameters(List.of()).withStackTrace(List.of()).withAttributes(new ArrayList<>());
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
}