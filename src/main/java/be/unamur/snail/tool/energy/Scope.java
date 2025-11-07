package be.unamur.snail.tool.energy;

public enum Scope {
    APP, ALL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
