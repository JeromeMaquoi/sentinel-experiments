package be.unamur.snail.tool.energy;

import be.unamur.snail.spoon.constructor_instrumentation.HttpErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimpleHttpClientTest {
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;
    private SimpleHttpClient client;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
        client = new SimpleHttpClient(mockHttpClient);
    }

    @Test
    void postShouldReturnResponseBodyOnSuccessTest() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("Success");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String endpoint = "http://example.com/api";
        String payload = "{\"key\":\"value\"}";
        String response = client.post(endpoint, payload);

        assertEquals("Success", response);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest sentRequest = requestCaptor.getValue();
        assertEquals(endpoint, sentRequest.uri().toString());
        assertEquals("application/json", sentRequest.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void postShouldThrowExceptionWhenPayloadIsNullTest() {
        String endpoint = "http://example.com/api";
        assertThrows(IOException.class, () -> client.post(endpoint, null));
    }

    @Test
    void postShouldThrowHttpErrorExceptionOnErrorResponseTest() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String endpoint = "http://example.com/api";
        String payload = "{\"key\":\"value\"}";
        assertThrows(HttpErrorException.class, () -> client.post(endpoint, payload));
    }

    @Test
    void postShouldThrowHttpErrorExceptionOnStatusCodeBelow200Test() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(199);
        when(mockResponse.body()).thenReturn("Informational Response");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String endpoint = "http://example.com/api";
        String payload = "{\"key\":\"value\"}";
        assertThrows(HttpErrorException.class, () -> client.post(endpoint, payload));
    }
}