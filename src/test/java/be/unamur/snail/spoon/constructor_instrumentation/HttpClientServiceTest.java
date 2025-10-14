package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InvalidPropertiesFormatException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpClientServiceTest {
    private HttpClient httpClient;
    private HttpClientService service;
    private HttpResponse<String> response;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        response = mock(HttpResponse.class);
        service = new HttpClientService(httpClient);
    }

    @Test
    void postReturnsResponseBodyWhenStatusIs200Test() throws Exception {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("OK");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        String jsonPayload = "{\"key\":\"value\"}";
        String result = service.post("http://fake.api", jsonPayload);

        assertEquals("OK", result);
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void postThrowsHttpErrorExceptionWhenStatusIsLowerThan200Test() throws Exception {
        when(response.statusCode()).thenReturn(199);
        when(response.body()).thenReturn("OK");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertThrows(HttpErrorException.class, () -> service.post("http://fake.api", "{\"key\":\"value\"}"));
    }

    @Test
    void postThrowsHttpErrorExceptionWhenStatusIsGreaterThan300Test() throws Exception {
        when(response.statusCode()).thenReturn(300);
        when(response.body()).thenReturn("OK");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertThrows(HttpErrorException.class, () -> service.post("http://fake.api", "{\"key\":\"value\"}"));
    }

    @Test
    void postThrowsExceptionIfPayloadIsNullTest() {
        assertThrows(InvalidPropertiesFormatException.class, () -> service.post("http://fake.api", null));
    }
}