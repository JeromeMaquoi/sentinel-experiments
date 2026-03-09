package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;

public interface ConstructorContextSender {
    void send(ConstructorContext context);

    void sendBatch(List<ConstructorContext> contexts);
}
