package be.unamur.snail.utils;

import java.io.File;
import java.io.IOException;

public interface CommandRunner {
    Utils.CompletedProcess run(String command) throws IOException, InterruptedException;
    Utils.CompletedProcess run(String command, File projectRoot) throws IOException, InterruptedException;
}
