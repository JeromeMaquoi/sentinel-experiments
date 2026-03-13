package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoularJXFolderProcessorFactoryTest {
    private Config.ImportConfig importConfig;
    private Context context;
    private PipelineLogger logger;
    private JoularJXFolderProcessorFactory factory;

    @BeforeEach
    void setUp() {
        importConfig = mock(Config.ImportConfig.class);
        context = mock(Context.class);
        logger = mock(PipelineLogger.class);
        when(context.getLogger()).thenReturn(logger);

        factory = new JoularJXFolderProcessorFactory(importConfig);
    }

    @Test
    void createReturnsFolderProcessorWithCorrectDependenciesTest() {
        FolderProcessor processor = factory.create(context);

        assertNotNull(processor);
        assertInstanceOf(JoularJXFolderProcessor.class, processor);

        JoularJXFolderProcessor folderProcessor = (JoularJXFolderProcessor) processor;
        JoularJXFileProcessor fileProcessor = (JoularJXFileProcessor) getField(folderProcessor, "fileProcessor");
        assertNotNull(fileProcessor);

        Config.ImportConfig actualImportConfig =  (Config.ImportConfig) getField(fileProcessor, "importConfig");
        assertEquals(importConfig, actualImportConfig);

        PipelineLogger actualLogger = (PipelineLogger) getField(folderProcessor, "log");
        assertEquals(logger, actualLogger);
    }

    /**
     * Helper method to access private fields via reflection
     */
    private Object getField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            fail("Failed to access field " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
}