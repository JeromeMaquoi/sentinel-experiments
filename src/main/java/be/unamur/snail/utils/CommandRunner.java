package be.unamur.snail.utils;

import java.io.IOException;

public interface CommandRunner {
    Utils.CompletedProcess run(String command) throws IOException, InterruptedException;
}
