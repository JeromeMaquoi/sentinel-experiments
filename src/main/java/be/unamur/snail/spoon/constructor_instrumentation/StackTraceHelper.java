package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceHelper {
    private final StackTraceProvider stackTraceProvider;

    public StackTraceHelper(StackTraceProvider stackTraceProvider) {
        this.stackTraceProvider = stackTraceProvider;
    }

    public List<StackTraceElement> getFilteredStackTrace() {
        String prefix = System.getProperty("packagePrefix", System.getenv("PACKAGE_PREFIX"));
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Package prefix not set");
        }
        return new ArrayList<>(Arrays.stream(this.stackTraceProvider.getStackTrace())
                .filter(element -> element.getClassName().startsWith(prefix))
                .toList());
    }
}
