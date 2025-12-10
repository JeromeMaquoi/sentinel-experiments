package be.unamur.snail.utils;

import java.io.File;
import java.io.IOException;

public class SimpleCommandRunner implements CommandRunner {
    @Override
    public Utils.CompletedProcess run(String command) throws IOException, InterruptedException {
        return Utils.runCommand(command);
    }

    @Override
    public Utils.CompletedProcess run(String command, File projectRoot) throws IOException, InterruptedException {
        return Utils.runCommand(command, projectRoot.getAbsolutePath());
    }
}
