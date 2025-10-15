package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;
import java.util.Set;

public class ConstructorContext implements AstElement {
    private String fileName;
    private String className;
    private String methodName;
    private List<String> parameters;
    private Set<AttributeContext> attributes;
    private List<StackTraceElement> stackTrace;
    private String snapshot;

    public ConstructorContext() {}

    public ConstructorContext withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ConstructorContext withClassName(String className) {
        this.className = className;
        return this;
    }

    public ConstructorContext withMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public ConstructorContext withParameters(List<String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ConstructorContext withAttributes(Set<AttributeContext> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ConstructorContext withStackTrace(List<StackTraceElement> stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public ConstructorContext withSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Set<AttributeContext> getAttributes() {
        return attributes;
    }

    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void addAttribute(AttributeContext attribute) {
        this.attributes.add(attribute);
    }

    public boolean isEmpty() {
        return fileName == null && className==null && methodName==null;
    }

    public boolean isComplete() {
        return fileName != null && className != null && methodName != null && parameters != null && attributes != null & stackTrace != null;
    }

    @Override
    public String toString() {
        return "ConstructorContext{" +
                "fileName='" + fileName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + parameters +
                ", attributes=" + attributes +
                ", stackTrace=" + stackTrace +
                ", snapshot='" + snapshot + '\'' +
                '}';
    }
}
