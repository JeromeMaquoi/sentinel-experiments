package be.unamur.snail.tool;

import be.unamur.snail.core.Context;

public interface ToolReleaseFetcher {
    /**
     * Fetches and prepares a specific version of a tool defined in config.
     * @param context
     * @return the local installation directory of the tool
     * @throws Exception
     */
    String fetchRelease(Context context) throws Exception;
}
