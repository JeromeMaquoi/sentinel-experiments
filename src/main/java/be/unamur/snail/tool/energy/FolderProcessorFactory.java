package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Context;

public interface FolderProcessorFactory {
    FolderProcessor create(Context context);
}
