package be.unamur.snail.spoon.constructor_instrumentation;

import be.unamur.snail.config.Config;
import be.unamur.snail.exceptions.MissingConfigKeyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceHelper {
    private final StackTraceProvider stackTraceProvider;

    public StackTraceHelper(StackTraceProvider stackTraceProvider) {
        this.stackTraceProvider = stackTraceProvider;
    }

    public List<StackTraceElement> getFilteredStackTrace() {
        Config config = Config.getInstance();
        if (config.getProject().getPackagePrefix() == null || config.getProject().getPackagePrefix().isEmpty()) {
            throw new MissingConfigKeyException("package-prefix");
        }
        return new ArrayList<>(Arrays.stream(this.stackTraceProvider.getStackTrace())
                .filter(element -> element.getClassName().startsWith(config.getProject().getPackagePrefix()))
                .toList());
    }
}
