package be.unamur.snail.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private List<String> classPath;

    public List<String> getClassPath() {
        return classPath;
    }

    public void setClassPath(List<String> classPath) {
        this.classPath = classPath;
    }
}
