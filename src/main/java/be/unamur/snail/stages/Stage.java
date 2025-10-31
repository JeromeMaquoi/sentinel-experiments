package be.unamur.snail.stages;

import be.unamur.snail.core.Context;

public interface Stage {
    void execute(Context context) throws Exception;
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
