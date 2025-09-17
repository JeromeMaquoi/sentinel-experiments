package be.unamur.snail.core;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public String toString() {
        return "Context{" +
                "data=" + data +
                '}';
    }
}
