package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.Objects;

public class CommitSimpleInstrDTO {
    private String sha;
    private RepositorySimpleInstrDTO repository;

    public CommitSimpleInstrDTO() {}

    public CommitSimpleInstrDTO(String sha, RepositorySimpleInstrDTO repository) {
        this.sha = sha;
        this.repository = repository;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public RepositorySimpleInstrDTO getRepository() {
        return repository;
    }

    public void setRepository(RepositorySimpleInstrDTO repository) {
        this.repository = repository;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CommitSimpleInstrDTO that = (CommitSimpleInstrDTO) o;
        return Objects.equals(sha, that.sha) && Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha, repository);
    }

    @Override
    public String toString() {
        return "CommitSimpleInstrDTO{" +
                "sha='" + sha + '\'' +
                ", repository=" + repository +
                '}';
    }
}
