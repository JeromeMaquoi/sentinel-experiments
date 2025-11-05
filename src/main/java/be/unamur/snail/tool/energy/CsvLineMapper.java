package be.unamur.snail.tool.energy;

import be.unamur.snail.tool.energy.model.RunIterationDTO;

@FunctionalInterface
public interface CsvLineMapper<T> {
    T map(String line, RunIterationDTO iteration, String commitSha);
}
