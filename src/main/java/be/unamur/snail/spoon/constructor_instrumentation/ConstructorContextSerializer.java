package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;

public class ConstructorContextSerializer {

    public ConstructorContextSerializer() {}

    public String serialize(ConstructorContext context) {
        return toJson(context);
    }

    public String serializeList(List<ConstructorContext> contexts) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < contexts.size(); i++) {
            sb.append(toJson(contexts.get(i)));
            if (i < contexts.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJson(ConstructorContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"fileName\":").append(jsonString(ctx.getFileName())).append(",");
        sb.append("\"className\":").append(jsonString(ctx.getClassName())).append(",");
        sb.append("\"methodName\":").append(jsonString(ctx.getMethodName())).append(",");
        sb.append("\"parameters\":").append(jsonStringList(ctx.getParameters())).append(",");
        sb.append("\"attributes\":").append(jsonAttributeList(ctx.getAttributes())).append(",");
        sb.append("\"stacktrace\":").append(jsonStacktrace(ctx.getStacktrace())).append(",");
        sb.append("\"snapshot\":").append(jsonString(ctx.getSnapshot())).append(",");
        sb.append("\"commit\":").append(jsonCommit(ctx.getCommit()));
        sb.append("}");
        return sb.toString();
    }

    private String jsonCommit(CommitSimpleInstrDTO commit) {
        if (commit == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"sha\":").append(jsonString(commit.getSha())).append(",");
        sb.append("\"repository\":").append(jsonRepository(commit.getRepository()));
        sb.append("}");
        return sb.toString();
    }

    private String jsonRepository(RepositorySimpleInstrDTO repo) {
        if (repo == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":").append(jsonString(repo.getName())).append(",");
        sb.append("\"owner\":").append(jsonString(repo.getOwner()));
        sb.append("}");
        return sb.toString();
    }

    private String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            + "\"";
    }

    private String jsonStringList(List<String> list) {
        if (list == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(jsonString(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonAttributeList(List<AttributeContext> list) {
        if (list == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            AttributeContext a = list.get(i);
            sb.append("{");
            sb.append("\"name\":").append(jsonString(a.getName())).append(",");
            sb.append("\"type\":").append(jsonString(a.getType())).append(",");
            sb.append("\"actualType\":").append(jsonString(a.getActualType())).append(",");
            sb.append("\"rhs\":").append(jsonString(a.getRhs()));
            sb.append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonStacktrace(List<StackTraceElement> list) {
        if (list == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            StackTraceElement e = list.get(i);
            sb.append("{");
            sb.append("\"className\":").append(jsonString(e.getClassName())).append(",");
            sb.append("\"methodName\":").append(jsonString(e.getMethodName())).append(",");
            sb.append("\"fileName\":").append(jsonString(e.getFileName())).append(",");
            sb.append("\"lineNumber\":").append(e.getLineNumber());
            sb.append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
