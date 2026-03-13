package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.exceptions.MissingConfigKeyException;
import be.unamur.snail.services.MeasurementsImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportMeasurementsStageTest {
    @TempDir
    Path tempDir;
    private Config config;

    @BeforeEach
    void setUp() throws Exception {
        Path yaml = tempDir.resolve("config.yml");
        Files.writeString(yaml, """
            project:
              sub-project: ""
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: ""
            log:
              level: "DEBUG"
        """);
        Config.load(yaml.toString());
        config = Config.getInstance();
    }

    @Test
    void executeThrowsWhenRepoPathMissingTest() {
        Context context = mock(Context.class);

        MeasurementsImportService service  = mock(MeasurementsImportService.class);
        ImportMeasurementsStage stage = new ImportMeasurementsStage(Path.of("results"), service);
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));

        config.setRepoForTests(new Config.RepoConfig());
        assertThrows(MissingConfigKeyException.class, () -> stage.execute(context));
    }

    @Test
    void executeCallsServiceTest() throws Exception {
        MeasurementsImportService service = mock(MeasurementsImportService.class);
        ImportMeasurementsStage stage = new ImportMeasurementsStage(Path.of("results"), service);
        Context context = mock(Context.class);

        config.getRepo().setTargetDirForTests("/repo");

        stage.execute(context);

        verify(service).importMeasurements(eq(Path.of("results")), eq("/repo"), eq(context));
    }
}