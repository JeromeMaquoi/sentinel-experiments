package be.unamur.snail.utils.parser;

import be.unamur.snail.exceptions.TimestampNotFoundException;
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

    @Test
    void extractTimestampShouldReturnCorrectTimestampWhenValidTest() {
        Path path = Path.of("joularJX-69227-1762168265789-filtered-methods.csv");
        long timestamp = JoularJXPathParser.extractTimestamp(path);
        assertEquals(1762168265789L, timestamp);
    }

    @Test
    void extractTimestampShouldWorkWithDifferentFileSuffixTest() {
        Path path = Path.of("joularJX-12345-9876543210123-filtered-calltrees.txt");
        long timestamp = JoularJXPathParser.extractTimestamp(path);
        assertEquals(9876543210123L, timestamp);
    }

    @Test
    void extractTimestampShouldThrowWhenMissingTimestampSectionTest() {
        Path path = Path.of("joularJX--filtered-runtime.csv");
        TimestampNotFoundException exception = assertThrows(TimestampNotFoundException.class, () -> {
            JoularJXPathParser.extractTimestamp(path);
        });
        assertTrue(exception.getMessage().contains("joularJX--filtered-runtime.csv"));
    }
}