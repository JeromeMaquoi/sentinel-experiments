package be.unamur.snail.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolReleaseFetcherFactoryTest {
    private ToolReleaseFetcherFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ToolReleaseFetcherFactory();
    }

    @Test
    void shouldCreateJoularJXFetcherTest() {
        assertInstanceOf(JoularJXFetcher.class, factory.createFectcher("joularjx"));
    }

    @Test
    void shouldThrowExceptionIfUnsupportedToolTest() {
        assertThrows(IllegalArgumentException.class, () -> factory.createFectcher("unsupportedtool"));
    }
}