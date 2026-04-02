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
     * Advances the bar by one step and records the name of the stage that is
     * about to execute.
     *
     * @param stageName human-readable stage name
     */
    public void advance(String stageName) {
        current.incrementAndGet();
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
     */
    public void stop() {
        activeBar = null;
        stopped   = true;
        scheduler.shutdownNow();
        synchronized (this) {
            doRender();
            out.println();
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

        // ── ETA ───────────────────────────────────────────────────────────
        String etaStr;
        if (done > 0 && total > 0) {
            long etaSecs = (elapsed.getSeconds() * (long)(total - done)) / done;
            etaStr = formatDuration(Duration.ofSeconds(etaSecs));
        } else {
            etaStr = "--:--:--";
        }

        // ── bar ───────────────────────────────────────────────────────────
        int filled = total > 0 ? (int) Math.round(((double) done / total) * BAR_WIDTH) : 0;
        int empty  = BAR_WIDTH - filled;
        String bar = GREEN + "█".repeat(filled) + DIM + "░".repeat(empty) + RESET;

        int pct = total > 0 ? (done * 100) / total : 0;

        // ── assemble line (no trailing newline – \r overwrites in place) ──
        String line = String.format(
                "\r[%s] %3d%% %2d/%-2d  " + CYAN + "%-40s" + RESET
                        + "  Elapsed: " + YELLOW + "%s" + RESET
                        + "  ETA: "     + YELLOW + "%s" + RESET + "   ",
                bar, pct, done, total,
                truncate(stageName, 40),
                formatDuration(elapsed),
                etaStr
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

