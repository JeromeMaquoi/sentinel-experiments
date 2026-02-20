package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;

public interface ConstructorContextSender {
    void send(ConstructorContext context);

    default void sendBatch(List<ConstructorContext> contexts) {
        for (ConstructorContext context : contexts) {
            send(context);
        }
    }
}
