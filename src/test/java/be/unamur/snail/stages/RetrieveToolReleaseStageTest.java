package be.unamur.snail.stages;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.logging.ConsolePipelineLogger;
import be.unamur.snail.tool.ToolReleaseFetcher;
import be.unamur.snail.tool.ToolReleaseFetcherFactory;
import be.unamur.snail.tool.ToolReleaseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetrieveToolReleaseStageTest {

    private ToolReleaseFetcherFactory mockFactory;
    private ToolReleaseFetcher mockFetcher;
    private Config mockConfig;
    private Context context;
    private RetrieveToolReleaseStage stage;

    @BeforeEach
    void setUp() {
        mockFactory = mock(ToolReleaseFetcherFactory.class);
        mockFetcher = mock(ToolReleaseFetcher.class);
        mockConfig  = mock(Config.class);

        Config.ExecutionPlanConfig mockPlan  = mock(Config.ExecutionPlanConfig.class);
        Config.EnergyMeasurementConfig mockEnergy = mock(Config.EnergyMeasurementConfig.class);
        when(mockConfig.getExecutionPlan()).thenReturn(mockPlan);
        when(mockPlan.getEnergyMeasurements()).thenReturn(mockEnergy);
        when(mockEnergy.getTool()).thenReturn("joularjx");

        when(mockFactory.createFectcher("joularjx")).thenReturn(mockFetcher);

        context = new Context();
        context.setLogger(new ConsolePipelineLogger(RetrieveToolReleaseStage.class));

        stage = new RetrieveToolReleaseStage(mockFactory, mockConfig);
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void executeSetsTookPathInContextTest() throws Exception {
        when(mockFetcher.fetchRelease()).thenReturn(new ToolReleaseResult("tools/dir", "2.0.0"));

        stage.execute(context);

        assertEquals("tools/dir/joularjx-2.0.0.jar", context.getEnergyToolPath());
    }

    /**
     * The tool path must follow the pattern {@code <path>/<toolName>-<version>.jar}
     * for any combination of path, version, and tool name.
     */
    @ParameterizedTest(name = "{0} + {2} {1} → {0}/{2}-{1}.jar")
    @CsvSource({
        "tools/dir,   2.0.0, joularjx",
        "/opt/tools,  1.5.2, joularjx",
        "/home/user,  3.0.1, joularjx"
    })
    void toolPathIsAssembledFromPathVersionAndToolNameTest(String path, String version, String toolName)
            throws Exception {
        // Reconfigure the mocks for the specific tool name used in this case
        Config.ExecutionPlanConfig plan = mock(Config.ExecutionPlanConfig.class);
        Config.EnergyMeasurementConfig energy = mock(Config.EnergyMeasurementConfig.class);
        when(mockConfig.getExecutionPlan()).thenReturn(plan);
        when(plan.getEnergyMeasurements()).thenReturn(energy);
        when(energy.getTool()).thenReturn(toolName);
        when(mockFactory.createFectcher(toolName)).thenReturn(mockFetcher);
        when(mockFetcher.fetchRelease()).thenReturn(new ToolReleaseResult(path.trim(), version.trim()));

        stage = new RetrieveToolReleaseStage(mockFactory, mockConfig);
        stage.execute(context);

        assertEquals(path.trim() + "/" + toolName.trim() + "-" + version.trim() + ".jar",
                context.getEnergyToolPath());
    }

    // ── factory / fetcher interaction ─────────────────────────────────────────

    @Test
    void executeInvokesFactoryWithToolNameFromConfigTest() throws Exception {
        when(mockFetcher.fetchRelease()).thenReturn(new ToolReleaseResult("some/path", "1.0"));

        stage.execute(context);

        verify(mockFactory).createFectcher("joularjx");
    }

    @Test
    void executeFetchesReleaseFromFetcherTest() throws Exception {
        when(mockFetcher.fetchRelease()).thenReturn(new ToolReleaseResult("some/path", "1.0"));

        stage.execute(context);

        verify(mockFetcher).fetchRelease();
    }

    // ── error handling ────────────────────────────────────────────────────────

    @Test
    void executePropagatesExceptionThrownByFetcherTest() throws Exception {
        when(mockFetcher.fetchRelease()).thenThrow(new RuntimeException("network error"));

        assertThrows(RuntimeException.class, () -> stage.execute(context),
                "Exceptions thrown by fetchRelease() must propagate to the caller");
    }
}

