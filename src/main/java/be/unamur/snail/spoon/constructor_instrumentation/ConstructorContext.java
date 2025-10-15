package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;

public class ConstructorContext implements AstElement {
    private String fileName;
    private String className;
    private String methodName;
    private List<String> parameters;
    private List<AttributeContext> attributes;
    private List<StackTraceElement> stacktrace;
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

    public ConstructorContext withAttributes(List<AttributeContext> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ConstructorContext withStackTrace(List<StackTraceElement> stacktrace) {
        this.stacktrace = stacktrace;
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

    public List<AttributeContext> getAttributes() {
        return attributes;
    }

    public List<StackTraceElement> getStacktrace() {
        return stacktrace;
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
        return fileName != null && className != null && methodName != null && parameters != null && attributes != null & stacktrace != null;
    }

    @Override
    public String toString() {
        return "ConstructorContext{" +
                "fileName='" + fileName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + parameters +
                ", attributes=" + attributes +
                ", stacktrace=" + stacktrace +
                ", snapshot='" + snapshot + '\'' +
                '}';
    }
}
