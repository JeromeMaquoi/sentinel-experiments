package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.*;

/**
 * This class is responsible for creating a batch of ConstructorContext to send to the db
 */
public class ConstructorEventDispatcher {
    private final int BATCH_SIZE;
    private final List<ConstructorContext> batch;
    private final ConstructorContextSender sender;
    private static volatile ConstructorEventDispatcher instance;
    private final Set<String> keysInBatch = new HashSet<>();

    public static synchronized ConstructorEventDispatcher getInstance(String apiUrl) {
        if (instance == null) {
            instance = new ConstructorEventDispatcher(apiUrl);
        }
        return instance;
    }

    private ConstructorEventDispatcher(String apiUrl) {
        this(new HttpConstructorContextSender(apiUrl), 500);
    }

    public ConstructorEventDispatcher(ConstructorContextSender sender, int batchSize) {
        this.sender = sender;
        this.BATCH_SIZE = batchSize;
        batch = new ArrayList<>(BATCH_SIZE);
        registerShutdownHook();
    }

    public synchronized void submit(ConstructorContext context) {
        String key = computeUniqueKey(context);
        if (!keysInBatch.contains(key)) {
            batch.add(context);
            keysInBatch.add(key);
        }

        if (batch.size() >= BATCH_SIZE) {
            flush();
        }
    }

    protected void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (ConstructorEventDispatcher.this) {
                flush();
            }
        }));
    }

    protected synchronized void flush() {
        if (batch.isEmpty()) return;

        sender.sendBatch(new ArrayList<>(batch));
        batch.clear();
        keysInBatch.clear();
    }

    protected String computeUniqueKey(ConstructorContext context) {
        int stacktraceHash = context.getStacktrace() != null ? context.getStacktrace().hashCode() : 0;
        return context.getFileName() + "|" + context.getClassName() + "|" + context.getMethodName() + "|" + (context.getParameters() == null ? "" : context.getParameters().toString()) + "|" + stacktraceHash;
    }

    protected List<ConstructorContext> getBatch() {
        return batch;
    }
}
