package be.unamur.snail.logging;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ProgressBarAwareConsoleAppender}.
 *
 * <p>{@link ProgressBar#getActive()} is a static method, so each test that
 * exercises the routing logic wraps its assertions in a
 * {@link MockedStatic} scope to control what the appender sees without
 * touching global state.
 *
 * <p>{@code System.out} is redirected to a {@link ByteArrayOutputStream}
 * in tests that verify the no-bar path, and is always restored in
 * {@link #tearDown()}.
 */
class ProgressBarAwareConsoleAppenderTest {

    @SuppressWarnings("unchecked")
    private final Layout<Serializable> mockLayout = mock(Layout.class);
    private final LogEvent mockEvent = mock(LogEvent.class);

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedSystemOut;
    private ProgressBarAwareConsoleAppender appender;

    @BeforeEach
    void setUp() {
        originalOut       = System.out;
        capturedSystemOut = new ByteArrayOutputStream();

        appender = ProgressBarAwareConsoleAppender.createAppender("TestAppender", mockLayout, null);
        appender.start();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (appender != null) {
            appender.stop();
        }
    }

    // ── createAppender factory ────────────────────────────────────────────────

    @Test
    void createAppenderWithNullNameReturnsNullTest() {
        assertNull(ProgressBarAwareConsoleAppender.createAppender(null, mockLayout, null),
                "Factory must return null when no name is provided");
    }

    @Test
    void createAppenderWithNullLayoutUsesDefaultLayoutTest() {
        ProgressBarAwareConsoleAppender a =
                ProgressBarAwareConsoleAppender.createAppender("name", null, null);
        assertNotNull(a, "Factory must produce an appender even when layout is null");
        assertNotNull(a.getLayout(), "A default layout must be applied when none is configured");
    }

    // ── append — no active bar ────────────────────────────────────────────────

    @Test
    void appendWritesMessageToSystemOutWhenNoBarIsActiveTest() {
        when(mockLayout.toByteArray(mockEvent)).thenReturn("log message".getBytes());
        System.setOut(new PrintStream(capturedSystemOut));

        try (MockedStatic<ProgressBar> staticBar = mockStatic(ProgressBar.class)) {
            staticBar.when(ProgressBar::getActive).thenReturn(null);
            appender.append(mockEvent);
        }

        assertTrue(capturedSystemOut.toString().contains("log message"),
                "Message should be written to System.out when no progress bar is running");
    }

    @Test
    void appendDoesNotProduceDoubleNewlineWhenLayoutIncludesOneTest() {
        // Layout already appends \n (as PatternLayout does with %n).
        // stripTrailing() removes it; println adds exactly one — so the output
        // must contain exactly one trailing newline, not two.
        when(mockLayout.toByteArray(mockEvent)).thenReturn("line\n".getBytes());
        System.setOut(new PrintStream(capturedSystemOut));

        try (MockedStatic<ProgressBar> staticBar = mockStatic(ProgressBar.class)) {
            staticBar.when(ProgressBar::getActive).thenReturn(null);
            appender.append(mockEvent);
        }

        String out = capturedSystemOut.toString().replace("\r\n", "\n");
        assertEquals("line\n", out,
                "Output must contain exactly one newline — not a double newline from layout + println");
    }

    // ── append — active bar ───────────────────────────────────────────────────

    @Test
    void appendRoutesThroughPrintAboveWhenBarIsActiveTest() {
        when(mockLayout.toByteArray(mockEvent)).thenReturn("log message".getBytes());
        ProgressBar mockBar = mock(ProgressBar.class);

        try (MockedStatic<ProgressBar> staticBar = mockStatic(ProgressBar.class)) {
            staticBar.when(ProgressBar::getActive).thenReturn(mockBar);
            appender.append(mockEvent);
        }

        verify(mockBar).printAbove("log message");
    }

    @Test
    void appendStripsTrailingNewlineBeforeCallingPrintAboveTest() {
        // Layout returns "message\n"; the appender must strip it before handing
        // the string to printAbove, which adds its own newline via println.
        when(mockLayout.toByteArray(mockEvent)).thenReturn("message\n".getBytes());
        ProgressBar mockBar = mock(ProgressBar.class);

        try (MockedStatic<ProgressBar> staticBar = mockStatic(ProgressBar.class)) {
            staticBar.when(ProgressBar::getActive).thenReturn(mockBar);
            appender.append(mockEvent);
        }

        verify(mockBar).printAbove("message");
    }

    @Test
    void appendDoesNotWriteToSystemOutWhenBarIsActiveTest() {
        when(mockLayout.toByteArray(mockEvent)).thenReturn("log message".getBytes());
        ProgressBar mockBar = mock(ProgressBar.class);
        System.setOut(new PrintStream(capturedSystemOut));

        try (MockedStatic<ProgressBar> staticBar = mockStatic(ProgressBar.class)) {
            staticBar.when(ProgressBar::getActive).thenReturn(mockBar);
            appender.append(mockEvent);
        }

        assertTrue(capturedSystemOut.toString().isEmpty(),
                "Nothing should be written to System.out when a progress bar is active");
    }
}

