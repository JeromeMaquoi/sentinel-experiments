package be.unamur.snail.tool.energy;

import be.unamur.snail.core.Config;
import be.unamur.snail.tool.energy.model.CommitSimpleDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoularJXMapperTest {
    @Test
    void mapScopeValidTest() {
        assertEquals(Scope.APP, JoularJXMapper.mapScope("app"));
        assertEquals(Scope.ALL, JoularJXMapper.mapScope("all"));
        assertEquals(Scope.ALL, JoularJXMapper.mapScope("ALL"));
    }

    @Test
    void mapScopeInvalidTest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXMapper.mapScope("invalid");
        });
        assertTrue(exception.getMessage().contains("Unknown scope"));
    }

    @Test
    void mapMeasurementLevelValidTest() {
        assertEquals(MeasurementLevel.RUNTIME, JoularJXMapper.mapMeasurementLevel("runtime"));
        assertEquals(MeasurementLevel.TOTAL, JoularJXMapper.mapMeasurementLevel("total"));
        assertEquals(MeasurementLevel.TOTAL, JoularJXMapper.mapMeasurementLevel("TOTAL"));
    }

    @Test
    void mapMeasurementLevelInvalidTest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXMapper.mapMeasurementLevel("invalid");
        });
        assertTrue(exception.getMessage().contains("Unknown measurement type"));
    }

    @Test
    void mapMonitoringTypeValidTest() {
        assertEquals(MonitoringType.CALLTREES, JoularJXMapper.mapMonitoringType("calltrees"));
        assertEquals(MonitoringType.METHODS, JoularJXMapper.mapMonitoringType("methods"));
        assertEquals(MonitoringType.METHODS, JoularJXMapper.mapMonitoringType("METHODS"));
    }

    @Test
    void mapMonitoringTypeInvalidTest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            JoularJXMapper.mapMonitoringType("invalid");
        });
        assertTrue(exception.getMessage().contains("Unknown monitoring type"));
    }

    @Test
    void mapCommitTest() {
        Config.ProjectConfig project = new Config.ProjectConfig();
        project.setOwnerForTests("myOwner");
        project.setNameForTests("myRepo");

        Config.RepoConfig repo = new Config.RepoConfig();
        repo.setCommitForTests("abc123");

        Config config = new Config();
        config.setProjectForTests(project);
        config.setRepoForTests(repo);

        Config.setInstanceForTests(config);

        CommitSimpleDTO dto = JoularJXMapper.mapCommit();
        assertEquals("abc123", dto.getSha());
        assertEquals("myOwner", dto.getRepository().getOwner());
        assertEquals("myRepo", dto.getRepository().getName());

        Config.reset();
    }
}