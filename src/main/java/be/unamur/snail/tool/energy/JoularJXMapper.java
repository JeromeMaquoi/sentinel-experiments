package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.tool.energy.model.CallTreeMeasurementDTO;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import be.unamur.snail.tool.energy.model.RepositorySimpleDTO;
import be.unamur.snail.tool.energy.model.RunIterationDTO;

import java.util.List;

public class JoularJXMapper {
    public static CallTreeMeasurementDTO mapCallTreelLine(String line, RunIterationDTO iteration, String commitSha, String scope) {
        String[] split = line.split(",");
        if (split.length < 2) return null;

        List<String> callstack = List.of(split[0].split(";"));
        float value = Float.parseFloat(split[1]);

        CallTreeMeasurementDTO dto = new CallTreeMeasurementDTO();
        dto.setIteration(iteration);
        dto.setCommit(createCommitSimpleDTO(commitSha));
        dto.setCallstack(callstack);
        dto.setValue(value);
        dto.setScope();
    }

    public static CommitSimpleDTO createCommitSimpleDTO(String commitSha) {
        CommitSimpleDTO commit = new CommitSimpleDTO();
        commit.setSha(commitSha);

        Config config = Config.getInstance();
        RepositorySimpleDTO repository = new RepositorySimpleDTO();
        repository.setName(config.getProject().getName());
        repository.setOwner(config.getProject().getOwner());

        commit.setRepository(repository);
        return commit;
    }
}