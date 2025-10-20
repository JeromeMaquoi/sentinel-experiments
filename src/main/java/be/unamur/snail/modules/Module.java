package be.unamur.snail.modules;

import be.unamur.snail.core.Context;

public interface Module {
    public void run(Context context) throws Exception;
}
