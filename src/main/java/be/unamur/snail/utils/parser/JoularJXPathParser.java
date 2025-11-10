package be.unamur.snail.utils.parser;

import java.nio.file.Path;
import java.util.List;

public class JoularJXPathParser {
    public record PathInfo(String scope, String measurementLevel, String monitoringType) {
    }

    public static PathInfo parse(Path path) {
        List<String> parts = List.of(path.toAbsolutePath().toString().split("/"));

        String monitoringType = findLast(parts, List.of("methods", "calltrees"));
        String measurementType = findLast(parts, List.of("runtime", "total"));
        String scope = findLast(parts, List.of("app", "all"));

        if (scope == null || measurementType == null || monitoringType == null) {
            throw new IllegalArgumentException("Unable to extract JoularJX folder structure from path: " + path);
        }
        return new PathInfo(scope, measurementType, monitoringType);
    }

    public static String findLast(List<String> parts, List<String> candidates) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            String name = parts.get(i);
            if (candidates.contains(name)) {
                return name;
            }
        }
        return null;
    }
}
