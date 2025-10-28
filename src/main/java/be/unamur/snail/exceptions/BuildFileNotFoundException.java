package be.unamur.snail.exceptions;

public class BuildFileNotFoundException extends RuntimeException {
    public BuildFileNotFoundException(String project, String subProject) {
        super("No build file found for " + project + "/" + subProject);
    }
}
