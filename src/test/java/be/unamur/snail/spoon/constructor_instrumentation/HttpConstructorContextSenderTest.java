package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HttpConstructorContextSenderTest {
    private HttpClientService client;
    private HttpConstructorContextSender sender;
    private ConstructorContext context;

    @BeforeEach
    void setUp() {
        client = mock(HttpClientService.class);
        sender = new HttpConstructorContextSender(client, "http://fake.api");
        context = new ConstructorContext()
                .withClassName("TestClass")
                .withMethodName("TestConstructor");
    }

    @Test
    void sendThrowsExceptionIfSendFailedTest() throws IOException, InterruptedException {
        when(client.post(anyString(), anyString())).thenThrow(new RuntimeException());
        assertThrows(ConstructorContextSendFailedException.class, () -> sender.send(context));
    }

    @Test
    void sendDoesNotThrowExceptionIfSendSucceededTest() throws IOException, InterruptedException {
        when(client.post(anyString(), anyString())).thenReturn("ok");
        assertDoesNotThrow(() -> sender.send(context));
        verify(client, times(1)).post(eq("http://fake.api"), contains("TestClass"));
    }
}