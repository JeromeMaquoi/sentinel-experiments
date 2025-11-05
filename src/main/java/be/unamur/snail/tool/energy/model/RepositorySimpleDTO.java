package be.unamur.snail.tool.energy.model;

import java.util.Objects;

public class RepositorySimpleDTO {
    private String name;
    private String owner;


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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositorySimpleDTO that = (RepositorySimpleDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }

    @Override
    public String toString() {
        return "RepositoryDTO{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}