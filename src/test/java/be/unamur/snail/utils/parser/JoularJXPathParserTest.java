package be.unamur.snail.utils.parser;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JoularJXPathParserTest {
    @Test
    void parseTypicalPathTest() {
        Path path = Path.of("/root/joularjx-result/69227-1762168265789/app/total/methods/file.csv");
        JoularJXPathParser.PathInfo info = JoularJXPathParser.parse(path);
        assertEquals("app", info.scope());
        assertEquals("total", info.measurementLevel());
        assertEquals("methods", info.monitoringType());
    }

    @Test
    void parsePathWithoutScopeTest() {
        Path path = Path.of("/root/joularjx-result/69227-1762168265789/total/methods/file.csv");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXPathParser.parse(path);
        });
        String expectedMessage = "Unable to extract JoularJX folder structure from path";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void parsePathWithoutMeasurementTypeTest() {
        Path path = Path.of("/root/joularjx-result/69227-1762168265789/app/methods/file.csv");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXPathParser.parse(path);
        });
        String expectedMessage = "Unable to extract JoularJX folder structure from path";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void parsePathWithoutMonitoringTypeTest() {
        Path path = Path.of("/root/joularjx-result/69227-1762168265789/app/total/file.csv");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXPathParser.parse(path);
        });
        String expectedMessage = "Unable to extract JoularJX folder structure from path";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}