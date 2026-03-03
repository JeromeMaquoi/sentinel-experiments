package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class is responsible for creating a batch of ConstructorContext to send to the db
 */
public class ConstructorEventDispatcher {
    private static final int BATCH_SIZE = 500;

    private final List<ConstructorContext> batch = new ArrayList<>(BATCH_SIZE);
    private final ConstructorContextSender sender;

    private static volatile ConstructorEventDispatcher instance;

    public static synchronized ConstructorEventDispatcher getInstance(String apiUrl) {
        if (instance == null) {
            instance = new ConstructorEventDispatcher(apiUrl);
        }
        return instance;
    }

    private ConstructorEventDispatcher(String apiUrl) {
        this.sender = new HttpConstructorContextSender(apiUrl);
        registerShutdownHook();
    }

    public synchronized void submit(ConstructorContext context) {
        batch.add(context);

        if (batch.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (ConstructorEventDispatcher.this) {
                flush();
            }
        }));
    }

    private synchronized void flush() {
        if (batch.isEmpty()) {
            return;
        }

        List<ConstructorContext> toSend = new ArrayList<>(batch);
        batch.clear();
        sender.sendBatch(toSend);
    }
}
