package be.unamur.snail.spoon.constructor_instrumentation;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SendConstructorsUtilsTest {
    private SendConstructorsUtils constructorUtils;

    @BeforeEach
    void setUp() {
        constructorUtils = new SendConstructorsUtils();
    }

    @Test
    void initConstructorContextStoresCorrectContextTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        ConstructorContext context = constructorUtils.getConstructorContextForTests();

        assertNotNull(context);
        assertEquals("file.java", context.getFileName());
        assertEquals("Class", context.getClassName());
        assertEquals("method", context.getMethodName());
        assertEquals(List.of("java.lang.String"), context.getParameters());
    }

    @Test
    void addAttributeWorkingTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.addAttribute("field", "String", "hello", "literal");

        ConstructorContext context = constructorUtils.getConstructorContextForTests();
        assertNotNull(context);
        assertEquals(1, context.getAttributes().size());
        AttributeContext attribute = context.getAttributes().iterator().next();
        assertEquals("field", attribute.getName());
        assertEquals("String", attribute.getType());
        assertEquals("java.lang.String", attribute.getActualType());
    }

    @Test
    void addAttributeThrowsExceptionIfConstructorContextNotInitializedTest() {
        assertThrows(IllegalStateException.class, () -> constructorUtils.addAttribute("field", "String", "hello", "literal"));
    }
}