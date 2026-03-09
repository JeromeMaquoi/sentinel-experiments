package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ConstructorEventDispatcherTest {
    private ConstructorEventDispatcher dispatcher;
    private ConstructorContextSender sender;

    @BeforeEach
    void setUp() {
        sender = mock(ConstructorContextSender.class);
        dispatcher = new ConstructorEventDispatcher(sender, 3);
    }

    private ConstructorContext createContext(String id) {
        return new ConstructorContext()
                .withFileName("File " + id)
                .withClassName("Class")
                .withMethodName("method")
                .withParameters(Collections.singletonList("param"));
    }

    @Test
    void submitShouldAddContextToBatchWithoutFlushTest() {
        dispatcher.submit(createContext("1"));
        verify(sender, never()).sendBatch(argThat(batch -> batch.size() == 1 && batch.get(0).getFileName().equals("File 1")));
    }

    @Test
    void submitShouldFlushWhenBatchSizeReachedTest() {
        dispatcher.submit(createContext("1"));
        dispatcher.submit(createContext("2"));
        dispatcher.submit(createContext("3"));

        verify(sender, times(1)).sendBatch(argThat(batch -> batch.size() == 3 && batch.get(0).getFileName().equals("File 1") && batch.get(1).getFileName().equals("File 2") && batch.get(2).getFileName().equals("File 3")));
    }

    @Test
    void submitShouldNotAddDuplicateContextsTest() {
        ConstructorContext context = createContext("1");
        dispatcher.submit(context);
        dispatcher.submit(context);

        assertEquals(1, dispatcher.getBatch().size());
    }

    @Test
    void submitShouldStartNewBatchAfterFlushTest() {
        dispatcher.submit(createContext("1"));
        dispatcher.submit(createContext("2"));
        dispatcher.submit(createContext("3"));

        dispatcher.submit(createContext("4"));
        dispatcher.flush();
        verify(sender, times(2)).sendBatch(any());
    }

    @Test
    void flushShouldNotSendEmptyBatchTest() {
        dispatcher.flush();
        verify(sender, never()).sendBatch(any());
    }

    @Test
    void flushShouldSendAllContextsWhenBatchNotEmptyTest() {
        dispatcher.submit(createContext("1"));
        dispatcher.submit(createContext("2"));

        dispatcher.flush();

        verify(sender).sendBatch(argThat(batch -> batch.size() == 2));
    }

    @Test
    void computeUniqueKeyShouldHandleNullStacktraceTest() {
        ConstructorContext context = createContext("1");
        context = context.withStackTrace(null);
        String key = dispatcher.computeUniqueKey(context);
        assertNotNull(key);
        assertTrue(key.contains("File 1"));
    }

    @Test
    void computeUniqueKeyShouldIncludeStacktraceHashTest() {
        ConstructorContext context = createContext("1").withStackTrace(Collections.singletonList(new StackTraceElement("Class", "method", "File 1", 10)));

        String key1 = dispatcher.computeUniqueKey(context);

        assertNotNull(key1);
    }

    @Test
    void computeUniqueKeyShouldHandleNullParametersTest() {
        ConstructorContext context = new ConstructorContext()
                .withFileName("File")
                .withClassName("Class")
                .withMethodName("method")
                .withParameters(null);

        String key = dispatcher.computeUniqueKey(context);
        assertNotNull(key);
        assertTrue(key.contains("File"));
    }

    @Test
    void singletonShouldReturnSameInstanceTest() {
        ConstructorEventDispatcher instance1 = ConstructorEventDispatcher.getInstance("http://a");
        ConstructorEventDispatcher instance2 = ConstructorEventDispatcher.getInstance("http://b");

        assertSame(instance1, instance2);
    }
}