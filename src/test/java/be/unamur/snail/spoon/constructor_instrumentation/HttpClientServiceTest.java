package be.unamur.snail.spoon.constructor_instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.InvalidPropertiesFormatException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpClientServiceTest {
    private SimpleHttpClient httpClient;
    private HttpClientService service;
    private SimpleHttpResponse response;

    @BeforeEach
    void setUp() {
        httpClient = mock(SimpleHttpClient.class);
        service = new HttpClientService(httpClient);
    }

    @Test
    void postReturnsResponseBodyWhenStatusIs200Test() throws Exception {
        response = new SimpleHttpResponse(200, "OK");
        when(httpClient.post(anyString(), anyString())).thenReturn(response);

        String jsonPayload = "{\"key\":\"value\"}";
        String result = service.post("http://fake.api", jsonPayload);

        assertEquals("OK", result);
        verify(httpClient, times(1)).post(anyString(), anyString());
    }

    @Test
    void postThrowsHttpErrorExceptionWhenStatusIsLowerThan200Test() throws Exception {
        response = new SimpleHttpResponse(199, "OK");
        when(httpClient.post(anyString(), anyString())).thenReturn(response);

        assertThrows(HttpErrorException.class, () -> service.post("http://fake.api", "{\"key\":\"value\"}"));
    }

    @Test
    void postThrowsHttpErrorExceptionWhenStatusIsGreaterThan300Test() throws Exception {
        response = new SimpleHttpResponse(300, "OK");
        when(httpClient.post(anyString(), anyString())).thenReturn(response);

        assertThrows(HttpErrorException.class, () -> service.post("http://fake.api", "{\"key\":\"value\"}"));
    }

    @Test
    void postThrowsExceptionIfPayloadIsNullTest() {
        assertThrows(InvalidPropertiesFormatException.class, () -> service.post("http://fake.api", null));
    }
}