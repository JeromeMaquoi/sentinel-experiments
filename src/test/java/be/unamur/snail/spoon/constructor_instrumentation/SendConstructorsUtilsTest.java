package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendConstructorsUtilsTest {
    private SendConstructorsUtils constructorUtils;
    private ConstructorContextSender sender;
    private ConstructorEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        StackTraceHelper mockHelper = mock(StackTraceHelper.class);
        StackTraceElement stackTraceElement = new StackTraceElement("org.springframework.boot.ApplicationEnvironmentTests", "createEnvironment", "ApplicationEnvironmentTests.java", 30);
        when(mockHelper.getFilteredStackTrace()).thenReturn(List.of(stackTraceElement));

        sender = mock(ConstructorContextSender.class);
        resetDispatcher();
        constructorUtils = new SendConstructorsUtils(mockHelper, sender);
    }

    @AfterEach
    void tearDown() {
        constructorUtils.resetConstructorContextForTests();
        resetDispatcher();
    }

    private void resetDispatcher() {
        try {
            var field = SendConstructorsUtils.class.getDeclaredField("dispatcher");
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
    void addAttributeThrowsExceptionIfConstructorContextIsNullTest() {
        constructorUtils.resetConstructorContextForTests();
        assertThrows(IllegalStateException.class, () -> constructorUtils.addAttribute("field", null, "hello", "literal"));
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
    void getStackTraceThrowsExceptionIfConstructorContextIsNullTest() {
        constructorUtils.resetConstructorContextForTests();
        assertThrows(IllegalStateException.class, () -> constructorUtils.getStackTrace());
    }

    @Test
    void getStackTraceWorkingTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.getStackTrace();

        ConstructorContext context = constructorUtils.getConstructorContextForTests();
        assertNotNull(context);
        List<StackTraceElement> stackTrace = context.getStacktrace();
        assertEquals(1, stackTrace.size());
        assertEquals("createEnvironment", stackTrace.get(0).getMethodName());
        assertEquals("ApplicationEnvironmentTests.java", stackTrace.get(0).getFileName());
        assertEquals("org.springframework.boot.ApplicationEnvironmentTests", stackTrace.get(0).getClassName());
        assertEquals(30, stackTrace.get(0).getLineNumber());
    }

    @Test
    void sendThrowsExceptionIfConstructorNotCompleteTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.addAttribute("field", "String", "hello", "literal");

        assertThrows(ConstructorContextNotCompletedException.class, () -> constructorUtils.send());
    }

    @Test
    void sendDelegatesToSenderWhenSenderIsNotNullTest() {
        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.addAttribute("field", "String", "hello", "literal");
        constructorUtils.getStackTrace();

        ConstructorContext context = constructorUtils.getConstructorContextForTests();
        constructorUtils.send();

        verify(sender, times(1)).send(context);
    }

    @Test
    void sendUsesDispatcherWhenSenderIsNullTest() {
        constructorUtils = new SendConstructorsUtils(mock(StackTraceHelper.class), null);

        dispatcher = mock(ConstructorEventDispatcher.class);
        setDispatcher(dispatcher);

        constructorUtils.initConstructorContext("file.java", "Class", "method", new ArrayList<>(List.of("java.lang.String")));
        constructorUtils.addAttribute("field", "String", "hello", "literal");
        constructorUtils.getStackTrace();

        ConstructorContext copy = constructorUtils.getConstructorContextForTests().copy();

        constructorUtils.send();

        verify(dispatcher, times(1)).submit(any(ConstructorContext.class));
    }

    @Test
    void initDispatcherInitializesDispatcherWhenNullTest() {
        assertNull(getDispatcher());
        System.setProperty("apiUrl", "http://localhost");
        SendConstructorsUtils.initDispatcher();
        assertNotNull(getDispatcher());
    }

    @Test
    void initDispatcherDoesNothingIfDispatcherAlreadySetTest() {
        System.setProperty("apiUrl", "http://localhost");
        SendConstructorsUtils.initDispatcher();
        ConstructorEventDispatcher firstInstance = getDispatcher();

        SendConstructorsUtils.initDispatcher();
        ConstructorEventDispatcher secondInstance = getDispatcher();

        assertSame(firstInstance, secondInstance);
    }

    @Test
    void initDispatcherThrowsExceptionIfApiUrlNotSetTest() {
        System.clearProperty("apiUrl");
        assertThrows(IllegalArgumentException.class, SendConstructorsUtils::initDispatcher);

        System.setProperty("apiUrl", "");
        assertThrows(IllegalArgumentException.class, SendConstructorsUtils::initDispatcher);
    }



    private void setDispatcher(ConstructorEventDispatcher dispatcher) {
        try {
            var field = SendConstructorsUtils.class.getDeclaredField("dispatcher");
            field.setAccessible(true);
            field.set(null, dispatcher);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ConstructorEventDispatcher getDispatcher() {
        try {
            var field = SendConstructorsUtils.class.getDeclaredField("dispatcher");
            field.setAccessible(true);
            return (ConstructorEventDispatcher) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}