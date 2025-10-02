package be.unamur.snail.utils;

import java.io.IOException;

public class SimpleCommandRunner implements CommandRunner {
    @Override
    public Utils.CompletedProcess run(String command) throws IOException, InterruptedException {
        return Utils.runCommand(command);
    }
}
