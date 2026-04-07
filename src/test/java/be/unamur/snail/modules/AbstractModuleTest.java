package be.unamur.snail.modules;

import be.unamur.snail.core.Context;
import be.unamur.snail.logging.PipelineLogger;
import be.unamur.snail.logging.ProgressBar;
import be.unamur.snail.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the stage-loop logic implemented in {@link AbstractModule#run(Context)}.
 *
 * <p>Because {@link AbstractModule} is abstract, each test instantiates a
 * minimal anonymous subclass via the {@link #module(Stage...)} helper.
 */
class AbstractModuleTest {

    private PipelineLogger mockLogger;
    private ProgressBar mockProgressBar;
    private Context context;

    @BeforeEach
    void setUp() {
        mockLogger      = mock(PipelineLogger.class);
        mockProgressBar = mock(ProgressBar.class);

        context = new Context();
        context.setLogger(mockLogger);
        // progress bar is NOT set by default; individual tests opt-in
    }

    // ── without progress bar ──────────────────────────────────────────────────

    @Test
    void runExecutesStagesInOrderTest() throws Exception {
        Stage s1 = mock(Stage.class);
        Stage s2 = mock(Stage.class);
        Stage s3 = mock(Stage.class);

        module(s1, s2, s3).run(context);

        InOrder order = inOrder(s1, s2, s3);
        order.verify(s1).execute(context);
        order.verify(s2).execute(context);
        order.verify(s3).execute(context);
    }

    @Test
    void runLogsStageStartAndEndForEachStageTest() throws Exception {
        Stage s1 = stageWithName("StageA");
        Stage s2 = stageWithName("StageB");

        module(s1, s2).run(context);

        verify(mockLogger).stageStart("StageA");
        verify(mockLogger).stageEnd("StageA");
        verify(mockLogger).stageStart("StageB");
        verify(mockLogger).stageEnd("StageB");
    }

    @Test
    void runLogsStartBeforeExecuteAndEndAfterTest() throws Exception {
        Stage stage = stageWithName("MyStage");

        module(stage).run(context);

        InOrder order = inOrder(mockLogger, stage);
        order.verify(mockLogger).stageStart("MyStage");
        order.verify(stage).execute(context);
        order.verify(mockLogger).stageEnd("MyStage");
    }

    @Test
    void runWithEmptyStageListCompletesNormallyTest() {
        assertDoesNotThrow(() -> module().run(context),
                "A module with no stages must complete without throwing");
    }

    @Test
    void runPropagatesExceptionFromStageTest() throws Exception {
        Stage failing = mock(Stage.class);
        doThrow(new RuntimeException("stage failed")).when(failing).execute(any());

        assertThrows(RuntimeException.class, () -> module(failing).run(context),
                "Exceptions thrown by a stage must propagate to the caller");
    }

    // ── with progress bar ─────────────────────────────────────────────────────

    @Test
    void runWithProgressBarCallsAllBarMethodsTest() throws Exception {
        context.setProgressBar(mockProgressBar);
        Stage s1 = stageWithName("S1");

        module(s1).run(context);

        verify(mockProgressBar).setLogger(mockLogger);
        verify(mockProgressBar).start(1);
        verify(mockProgressBar).setStageName("S1");
        verify(mockProgressBar).advance("S1");
        verify(mockProgressBar).stop();
    }

    @Test
    void runWithProgressBarWiresLoggerBeforeStartTest() throws Exception {
        context.setProgressBar(mockProgressBar);

        module().run(context);

        InOrder order = inOrder(mockProgressBar);
        order.verify(mockProgressBar).setLogger(mockLogger);
        order.verify(mockProgressBar).start(0);
    }

    @Test
    void runWithProgressBarSetNameBeforeExecuteAndAdvanceAfterTest() throws Exception {
        context.setProgressBar(mockProgressBar);
        Stage s1 = stageWithName("S1");

        module(s1).run(context);

        InOrder order = inOrder(mockProgressBar, s1);
        order.verify(mockProgressBar).setStageName("S1");
        order.verify(s1).execute(context);
        order.verify(mockProgressBar).advance("S1");
    }

    @Test
    void runWithProgressBarStartedWithCorrectStageCountTest() throws Exception {
        context.setProgressBar(mockProgressBar);

        module(mock(Stage.class), mock(Stage.class), mock(Stage.class)).run(context);

        verify(mockProgressBar).start(3);
    }

    @Test
    void runWithProgressBarAdvancedOncePerStageTest() throws Exception {
        context.setProgressBar(mockProgressBar);

        module(mock(Stage.class), mock(Stage.class)).run(context);

        verify(mockProgressBar, times(2)).advance(any());
    }

    @Test
    void runWithProgressBarStopCalledEvenWhenStageThrowsTest() throws Exception {
        context.setProgressBar(mockProgressBar);
        Stage failing = mock(Stage.class);
        doThrow(new RuntimeException("boom")).when(failing).execute(any());

        assertThrows(RuntimeException.class, () -> module(failing).run(context));

        verify(mockProgressBar).stop();
    }

    @Test
    void runWithNullProgressBarDoesNotThrowTest() throws Exception {
        // context has no progress bar set (null) — run() must guard every bar call
        Stage s = mock(Stage.class);
        assertDoesNotThrow(() -> module(s).run(context),
                "run() must handle a null progress bar without throwing");
        verify(s).execute(context);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Creates a minimal concrete {@link AbstractModule} wrapping the given stages. */
    private AbstractModule module(Stage... stages) {
        List<Stage> list = List.of(stages);
        return new AbstractModule() {
            @Override
            protected List<Stage> getStages() {
                return list;
            }
        };
    }

    /** Returns a mock {@link Stage} whose {@code getName()} returns {@code name}. */
    private Stage stageWithName(String name) {
        Stage s = mock(Stage.class);
        when(s.getName()).thenReturn(name);
        return s;
    }
}

