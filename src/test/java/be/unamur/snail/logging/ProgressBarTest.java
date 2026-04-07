package be.unamur.snail.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProgressBar}.
 *
 * <p>A {@link PrintStream} wrapping a {@link ByteArrayOutputStream} is injected
 * via the package-visible constructor so that every character written to the bar
 * can be captured and asserted without touching the real {@code System.out}.
 *
 * <p>Because {@link ProgressBar} keeps a static {@code activeBar} field,
 * {@link #tearDown()} always ensures it is cleared between tests so no test
 * can accidentally affect the next one.
 */
class ProgressBarTest {

    private ByteArrayOutputStream capturedOutput;
    private ProgressBar bar;

    @BeforeEach
    void setUp() {
        capturedOutput = new ByteArrayOutputStream();
        bar = new ProgressBar(new PrintStream(capturedOutput));
    }

    @AfterEach
    void tearDown() {
        // Guarantee the global active bar is cleared even if a test fails mid-run.
        ProgressBar active = ProgressBar.getActive();
        if (active != null) {
            active.stop();
        }
    }

    // ── getActive ─────────────────────────────────────────────────────────────

    @Test
    void getActiveReturnsNullWhenNoBarIsRunningTest() {
        assertNull(ProgressBar.getActive());
    }

    @Test
    void startRegistersBarAsActiveTest() {
        bar.start(5);
        assertSame(bar, ProgressBar.getActive(),
                "getActive() should return the bar after start()");
    }

    @Test
    void stopClearsActiveBarTest() {
        bar.start(5);
        bar.stop();
        assertNull(ProgressBar.getActive(),
                "getActive() should return null after stop()");
    }

    // ── output ────────────────────────────────────────────────────────────────

    @Test
    void startAndStopProducesOutputTest() {
        bar.start(3);
        bar.stop();
        assertFalse(output().isBlank(),
                "Bar should write something to the output stream between start() and stop()");
    }

    @Test
    void advanceIncrementsDisplayedCountTest() {
        bar.start(5);
        bar.advance("Stage1");
        bar.advance("Stage2");
        bar.stop();
        // Format: " %2d/%-2d " → " 2/5 " after two advances on a total of five
        assertTrue(output().contains("2/5"),
                "Output should show the completed count out of the total");
    }

    @Test
    void allStagesDoneShowsHundredPercentTest() {
        bar.start(2);
        bar.advance("Stage1");
        bar.advance("Stage2");
        bar.stop();
        assertTrue(output().contains("100%"),
                "Output should show 100% once every stage has been advanced through");
    }

    @Test
    void setStageNameAppearsInOutputTest() {
        bar.start(5);
        bar.setStageName("MySpecialStage");
        bar.stop();
        assertTrue(output().contains("MySpecialStage"),
                "Stage name set via setStageName() should be visible in the rendered output");
    }

    @Test
    void advanceStageNameAppearsInOutputTest() {
        bar.start(3);
        bar.advance("CompletedStage");
        bar.stop();
        assertTrue(output().contains("CompletedStage"),
                "Stage name passed to advance() should be visible in the rendered output");
    }

    @Test
    void outputContainsElapsedTimeLabelTest() {
        bar.start(3);
        bar.stop();
        assertTrue(output().contains("Elapsed:"),
                "Output should contain the 'Elapsed:' label");
    }

    @Test
    void outputContainsDurationInHhMmSsFormatTest() {
        bar.start(3);
        bar.stop();
        // The duration block always follows the format HH:MM:SS (e.g. 00:00:00)
        assertTrue(output().matches("(?s).*\\d{2}:\\d{2}:\\d{2}.*"),
                "Output should contain a duration formatted as HH:MM:SS");
    }

    // ── printAbove ────────────────────────────────────────────────────────────

    @Test
    void printAboveWritesMessageToOutputStreamTest() {
        bar.start(3);
        bar.printAbove("Important log line");
        bar.stop();
        assertTrue(output().contains("Important log line"),
                "Message passed to printAbove() should appear in the output stream");
    }

    // ── truncation ────────────────────────────────────────────────────────────

    @Test
    void stageNameLongerThan40CharsIsTruncatedTest() {
        String longName = "A".repeat(50);   // 50 chars — exceeds the 40-char display limit
        bar.start(3);
        bar.setStageName(longName);
        bar.stop();
        String out = output();
        assertTrue(out.contains("..."),
                "Stage names longer than 40 chars should be shortened and end with '...'");
        assertFalse(out.contains(longName),
                "The full long stage name must not appear verbatim in the output");
    }

    @Test
    void stageNameExactly40CharsIsNotTruncatedTest() {
        String name40 = "B".repeat(40);     // exactly at the limit — must not be truncated
        bar.start(3);
        bar.setStageName(name40);
        bar.stop();
        assertTrue(output().contains(name40),
                "A stage name of exactly 40 chars should appear in full without truncation");
    }

    @Test
    void stageNameShorterThan40CharsIsNotTruncatedTest() {
        String shortName = "ShortName";
        bar.start(3);
        bar.setStageName(shortName);
        bar.stop();
        assertTrue(output().contains(shortName),
                "A short stage name should appear verbatim in the output");
    }

    // ── null safety ───────────────────────────────────────────────────────────

    @Test
    void advanceWithNullStageNameDoesNotThrowTest() {
        bar.start(3);
        assertDoesNotThrow(() -> bar.advance(null),
                "advance(null) must not throw an exception");
        bar.stop();
    }

    @Test
    void setStageNameWithNullDoesNotThrowTest() {
        bar.start(3);
        assertDoesNotThrow(() -> bar.setStageName(null),
                "setStageName(null) must not throw an exception");
        bar.stop();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the captured output with ANSI escape sequences and carriage
     * returns stripped, so plain-text assertions can be written without
     * worrying about terminal control characters.
     */
    private String output() {
        return capturedOutput.toString()
                .replaceAll("\u001B\\[[^a-zA-Z]*[a-zA-Z]", "")
                .replace("\r", "");
    }
}

