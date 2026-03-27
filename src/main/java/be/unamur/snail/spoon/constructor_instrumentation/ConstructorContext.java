package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConstructorContext implements AstElement {
    private String fileName;
    private String className;
    private String methodName;
    private List<String> parameters;
    private List<AttributeContext> attributes;
    private List<StackTraceElement> stacktrace;
    private String snapshot;
    private CommitSimpleInstrDTO commit;

    public ConstructorContext() {}

    public ConstructorContext copy() {
        return new ConstructorContext()
                .withFileName(this.fileName)
                .withClassName(this.className)
                .withMethodName(this.methodName)
                .withParameters(this.parameters == null ? null : new ArrayList<>(this.parameters))
                .withAttributes(this.attributes == null ? null : new ArrayList<>(this.attributes))
                .withStackTrace(this.stacktrace == null ? null : new ArrayList<>(this.stacktrace))
                .withSnapshot(this.snapshot)
                .withCommit(this.commit);
    }

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

    public ConstructorContext withCommit(CommitSimpleInstrDTO commit) {
        this.commit = commit;
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

    public CommitSimpleInstrDTO getCommit() {
        return commit;
    }

    public void addAttribute(AttributeContext attribute) {
        this.attributes.add(attribute);
    }

    public boolean isEmpty() {
        return fileName == null && className==null && methodName==null;
    }

    public boolean isComplete() {
        return fileName != null && className != null && methodName != null && parameters != null && attributes != null && stacktrace != null && commit != null;
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
                ", commit=" + commit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorContext that = (ConstructorContext) o;
        return Objects.equals(fileName, that.fileName) && Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(parameters, that.parameters) && Objects.equals(attributes, that.attributes) && Objects.equals(stacktrace, that.stacktrace) && Objects.equals(snapshot, that.snapshot) && Objects.equals(commit, that.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, className, methodName, parameters, stacktrace, commit);
    }
}
