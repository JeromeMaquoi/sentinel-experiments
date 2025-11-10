package be.unamur.snail.utils.parser;

import be.unamur.snail.exceptions.TimestampNotFoundException;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static long extractTimestamp(Path path) {
        String fileName = path.getFileName().toString();
        Pattern pattern = Pattern.compile("joularJX-\\d+-(\\d+)-filtered-.*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new TimestampNotFoundException(fileName);
    }
}
