package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.*;

/**
 * This class is responsible for creating a batch of ConstructorContext to send to the db
 */
public class ConstructorEventDispatcher {
    private static final int BATCH_SIZE = 500;
    private final List<ConstructorContext> batch = new ArrayList<>(BATCH_SIZE);
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
        this.sender = new HttpConstructorContextSender(apiUrl);
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

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (ConstructorEventDispatcher.this) {
                flush();
            }
        }));
    }

    private synchronized void flush() {
        if (batch.isEmpty()) return;

        sender.sendBatch(new ArrayList<>(batch));
        batch.clear();
        keysInBatch.clear();
    }

    private String computeUniqueKey(ConstructorContext context) {
        int stacktraceHash = context.getStacktrace() != null ? context.getStacktrace().hashCode() : 0;
        return context.getFileName() + "|" + context.getClassName() + "|" + context.getMethodName() + "|" + (context.getParameters() == null ? "" : context.getParameters().toString()) + "|" + stacktraceHash;
    }
}
