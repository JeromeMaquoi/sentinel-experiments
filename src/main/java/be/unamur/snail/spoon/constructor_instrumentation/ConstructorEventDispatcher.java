package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for creating a batch of ConstructorContext to send to the db
 */
public class ConstructorEventDispatcher {
    private static final int BATCH_SIZE = 200;
    private static final int FLUSH_INTERVAL_MS = 1000;
    private static final int QUEUE_SIZE = 50000;

    private final BlockingQueue<ConstructorContext> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    private final ExecutorService worker;
    private final ConstructorContextSender sender;
    private static volatile ConstructorEventDispatcher instance;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public static synchronized ConstructorEventDispatcher getInstance(String apiUrl) {
        if (instance == null) {
            instance = new ConstructorEventDispatcher(apiUrl);
        }
        return instance;
    }

    private ConstructorEventDispatcher(String apiUrl) {
        this.worker = Executors.newSingleThreadExecutor();
        this.sender = new HttpConstructorContextSender(apiUrl);
        startWorker();
        registerShutdownHook();
    }

    public void submit(ConstructorContext context) {
        // never block constructors
        queue.offer(context);
    }

    private void startWorker() {
        worker.submit(() -> {
            List<ConstructorContext> batch = new ArrayList<>(BATCH_SIZE);
            long lastFlushTime = System.currentTimeMillis();

            while (running.get() || !queue.isEmpty()) {
                try {
                    // Wait for at least one item, but not more than FLUSH_INTERVAL_MS
                    ConstructorContext context = queue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);

                    if (context != null) {
                        batch.add(context);
                    }

                    // Drain remaining to fill the batch
                    queue.drainTo(batch, BATCH_SIZE - batch.size());

                    long now = System.currentTimeMillis();
                    boolean timeExceeded = now - lastFlushTime >= FLUSH_INTERVAL_MS;

                    // Only send if batch is not empty
                    if (!batch.isEmpty() && (batch.size() >= BATCH_SIZE || timeExceeded)) {
                        System.out.println("Sending batch of " + batch.size() + " constructor contexts");
                        sender.sendBatch(batch);
                        batch.clear();
                        lastFlushTime = now;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // flush any remaining contexts before exit
            flushRemaining();
        });
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            worker.shutdown();
            try {
                if (!worker.awaitTermination(5, TimeUnit.SECONDS)) {
                    worker.shutdownNow();
                }
            } catch (InterruptedException e) {
                worker.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void flushRemaining() {
        List<ConstructorContext> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            sender.sendBatch(remaining);
        }
    }
}
