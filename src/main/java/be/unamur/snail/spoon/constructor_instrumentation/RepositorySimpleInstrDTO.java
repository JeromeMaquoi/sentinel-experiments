package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.Objects;

public class RepositorySimpleInstrDTO {
    private String name;
    private String owner;

    public RepositorySimpleInstrDTO() {}

    public RepositorySimpleInstrDTO(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RepositorySimpleInstrDTO that = (RepositorySimpleInstrDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }

    @Override
    public String toString() {
        return "RepositorySimpleInstrDTO{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
