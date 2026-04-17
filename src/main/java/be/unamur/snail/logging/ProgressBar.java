package be.unamur.snail.logging;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Console-only progress bar that renders in-place using ANSI escape codes.
 * Displays pipeline progress, elapsed time and estimated time remaining.
 * <p>
 * Output is written exclusively to the console ({@code System.out} by default) –
 * nothing is sent to log files.
 * <p>
 * When a log message also needs to reach the console (via
 * {@link FilePipelineLogger} with {@code alsoLogToConsole = true}), call
 * {@link #printAbove(String)} to clear the bar line first, print the message,
 * then redraw the bar so both coexist cleanly.
 */
public class ProgressBar {

    // ── ANSI escape codes ──────────────────────────────────────────────────
    private static final String RESET      = "\u001B[0m";
    private static final String GREEN      = "\u001B[32m";
    private static final String CYAN       = "\u001B[36m";
    private static final String YELLOW     = "\u001B[33m";
    private static final String DIM        = "\u001B[2m";
    private static final String ERASE_LINE = "\u001B[2K";

    private static final int BAR_WIDTH = 30;

    /** Globally active progress bar, or {@code null} when none is running. */
    private static volatile ProgressBar activeBar = null;

    // ── state (AtomicXxx for cross-thread visibility) ──────────────────────
    private int total;
    private final AtomicInteger           current         = new AtomicInteger(0);
    private final AtomicReference<String> currentStageName = new AtomicReference<>("");
    private Instant                       startTime;

    private final PrintStream             out;
    private final ScheduledExecutorService scheduler;
    private volatile boolean              stopped = false;

    /**
     * Optional logger that receives a single summary line when {@link #stop()}
     * is called. Set via {@link #setLogger(PipelineLogger)} before
     * {@link #start(int)} so the total elapsed time is persisted to the log file.
     */
    private volatile PipelineLogger logger;

    // ── constructors ───────────────────────────────────────────────────────

    public ProgressBar() {
        this(System.out);
    }

    public ProgressBar(PrintStream out) {
        this.out = out;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "progress-bar");
            t.setDaemon(true);
            return t;
        });
    }

    // ── public API ─────────────────────────────────────────────────────────

    /**
     * Returns the currently active {@link ProgressBar}, or {@code null} when
     * no bar is running. Used by {@link FilePipelineLogger} to route console
     * messages through {@link #printAbove(String)}.
     */
    public static ProgressBar getActive() {
        return activeBar;
    }

    /**
     * Registers the {@link PipelineLogger} that will receive a one-line
     * completion summary (stage count and total elapsed time) when
     * {@link #stop()} is called. Call this before {@link #start(int)}.
     *
     * @param logger the pipeline logger backed by the log file; may be {@code null}
     *               to disable file logging of the summary
     */
    public void setLogger(PipelineLogger logger) {
        this.logger = logger;
    }

    /**
     * Starts the bar and its background refresh thread.
     *
     * @param totalStages total number of stages in the pipeline
     */
    public void start(int totalStages) {
        this.total     = totalStages;
        this.startTime = Instant.now();
        activeBar      = this;
        scheduler.scheduleAtFixedRate(this::renderScheduled, 0, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Advances the bar by one step and records the name of the completed stage.
     * Call this <em>after</em> a stage finishes so the counter only reaches
     * 100 % once every stage has completed.
     *
     * @param stageName human-readable stage name
     */
    public void advance(String stageName) {
        current.incrementAndGet();
        currentStageName.set(stageName != null ? stageName : "");
    }

    /**
     * Updates the displayed stage name <em>without</em> advancing the counter.
     * Call this <em>before</em> a stage starts so the name is visible while the
     * stage is executing, at the percentage of the <em>previous</em> stage.
     *
     * @param stageName human-readable stage name
     */
    public void setStageName(String stageName) {
        currentStageName.set(stageName != null ? stageName : "");
    }

    /**
     * Clears the current bar line, prints {@code message} on its own line, then
     * redraws the bar below it. This keeps log lines and the bar from
     * overwriting each other when console logging is enabled.
     *
     * @param message fully-formatted log line (no trailing newline needed)
     */
    public synchronized void printAbove(String message) {
        out.print("\r" + ERASE_LINE);   // erase the bar line
        out.println(message);           // log line (adds its own newline)
        doRender();                     // redraw bar on the fresh line
    }

    /**
     * Stops the refresh thread, renders a final completed frame and moves to
     * a new line so subsequent output starts cleanly below the bar.
     * If a logger was registered via {@link #setLogger(PipelineLogger)}, a
     * one-line completion summary is also written to the log file.
     */
    public void stop() {
        activeBar = null;
        stopped   = true;
        scheduler.shutdownNow();
        synchronized (this) {
            doRender();
            out.println();
        }
        if (logger != null) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            String summary = String.format("Pipeline complete: %d/%d stages  Elapsed: %s",
                    current.get(), total, formatDuration(elapsed));
            logger.info(summary);
        }
    }

    // ── private helpers ────────────────────────────────────────────────────

    /** Invoked by the scheduler every 200 ms. Holds the instance lock. */
    private synchronized void renderScheduled() {
        if (stopped) return;
        doRender();
    }

    /**
     * Core render logic. <strong>Must be called while holding {@code this}
     * monitor</strong> to avoid interleaving with {@link #printAbove}.
     */
    private void doRender() {
        int      done      = current.get();
        String   stageName = currentStageName.get();
        Duration elapsed   = Duration.between(startTime, Instant.now());

        // ── bar ───────────────────────────────────────────────────────────
        int filled = total > 0 ? (int) Math.round(((double) done / total) * BAR_WIDTH) : 0;
        int empty  = BAR_WIDTH - filled;
        String bar = GREEN + "█".repeat(filled) + DIM + "░".repeat(empty) + RESET;

        int pct = total > 0 ? (done * 100) / total : 0;

        // ── assemble line (no trailing newline – \r overwrites in place) ──
        String line = String.format(
                "\r[%s] %3d%% %2d/%-2d  " + CYAN + "%-40s" + RESET
                        + "  Elapsed: " + YELLOW + "%s" + RESET+ "   ",
                bar, pct, done, total,
                truncate(stageName, 40),
                formatDuration(elapsed)
        );

        out.print(line);
    }

    private static String formatDuration(Duration d) {
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }

    private static String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) return s == null ? "" : s;
        return s.substring(0, maxLen - 3) + "...";
    }
}

