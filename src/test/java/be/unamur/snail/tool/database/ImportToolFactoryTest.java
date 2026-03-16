package be.unamur.snail.tool.database;

import be.unamur.snail.core.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ImportToolFactoryTest {
    @Test
    void createReturnsJoularJXToolTest() {
        Config config = mock(Config.class);
        ImportToolFactory factory = new ImportToolFactory(config);
        ImportTool tool = factory.create("joularjx");
        assertInstanceOf(JoularJXImportTool.class, tool);
    }

    @Test
    void createThrowsExceptionForUnsupportedToolTest() {
        Config config = mock(Config.class);
        ImportToolFactory factory = new ImportToolFactory(config);
        assertThrows(IllegalArgumentException.class, () -> factory.create("unsupportedtool"));
    }
}