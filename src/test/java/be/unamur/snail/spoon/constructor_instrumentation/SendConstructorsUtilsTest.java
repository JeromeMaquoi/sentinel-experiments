package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SendConstructorsUtilsTest {
    private SendConstructorsUtils constructorUtils;

    @BeforeEach
    void setUp() {
        StackTraceHelper mockHelper = Mockito.mock(StackTraceHelper.class);
        StackTraceElement stackTraceElement = new StackTraceElement("org.springframework.boot.ApplicationEnvironmentTests", "createEnvironment", "ApplicationEnvironmentTests.java", 30);
        when(mockHelper.getFilteredStackTrace()).thenReturn(List.of(stackTraceElement));
        constructorUtils = new SendConstructorsUtils(mockHelper);
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

    @Test
    void addAttributeWithNullActualObjectTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.addAttribute("field", "int", null, "literal");

        ConstructorContext context = constructorUtils.getConstructorContextForTests();
        assertNotNull(context);
        assertEquals(1, context.getAttributes().size());
        AttributeContext attribute = context.getAttributes().iterator().next();
        assertEquals("field", attribute.getName());
        assertEquals("int", attribute.getType());
        assertEquals("null", attribute.getActualType());
    }

    @Test
    void getStackTraceThrowsExceptionIfConstructorContextNotInitializedTest() {
        assertThrows(IllegalStateException.class, () -> constructorUtils.getStackTrace());
    }

    @Test
    void getStackTraceWorkingTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.getStackTrace();

        ConstructorContext context = constructorUtils.getConstructorContextForTests();
        assertNotNull(context);
        List<StackTraceElement> stackTrace = context.getStackTrace();
        assertEquals(1, stackTrace.size());
        assertEquals("createEnvironment", stackTrace.get(0).getMethodName());
        assertEquals("ApplicationEnvironmentTests.java", stackTrace.get(0).getFileName());
        assertEquals("org.springframework.boot.ApplicationEnvironmentTests", stackTrace.get(0).getClassName());
        assertEquals(30, stackTrace.get(0).getLineNumber());
    }

    @Test
    void serializeSimpleConstructorContextWorkingTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        String json = constructorUtils.serializeConstructorContext();
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
                  "empty" : false
                }""", json);
    }
}