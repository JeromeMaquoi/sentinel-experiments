package be.unamur.snail.spoon.constructor_instrumentation;

public class AttributeContext {
    private final String name;
    private final String type;
    private final String actualType;
    private final String rhs;

    public AttributeContext(String name, String type, String actualType, String rhs) {
        this.name = name;
        this.type = type;
        this.actualType = actualType;
        this.rhs = rhs;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getActualType() {
        return actualType;
    }

    public String getRhs() {
        return rhs;
    }

    @Override
    public String toString() {
        return "AttributeContext{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", actualType='" + actualType + '\'' +
                ", rhs='" + rhs + '\'' +
                '}';
    }
}
