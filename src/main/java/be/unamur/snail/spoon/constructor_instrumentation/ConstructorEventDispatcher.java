package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class is responsible for creating a batch of ConstructorContext to send to the db
 */
public class ConstructorEventDispatcher {
    private static final int BATCH_SIZE = 200;
    private static final int FLUSH_INTERVAL_MS = 2000;
    private static final int QUEUE_SIZE = 50000;

    private final BlockingQueue<ConstructorContext> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    private final ExecutorService worker;
    private final ConstructorContextSender sender;
    private static volatile ConstructorEventDispatcher instance;

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

            while (true) {
                try {
                    ConstructorContext first = queue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);

                    if (first != null) {
                        batch.add(first);
                        queue.drainTo(batch, BATCH_SIZE - 1);
                    }
                    if (!batch.isEmpty()) {
                        sender.sendBatch(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // important not to crash the worker
                }
            }
        });
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }));
    }

    public void flush() {
        List<ConstructorContext> remaining = new ArrayList<>();
        queue.drainTo(remaining);

        if (!remaining.isEmpty()) {
            sender.sendBatch(remaining);
        }
    }
}
