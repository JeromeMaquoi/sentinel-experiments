package be.unamur.snail.exceptions;

public class JavaHomeNotFoundException extends RuntimeException {
    public JavaHomeNotFoundException(String jdkVersion) {
        super("Could not determine JAVA_HOME for JDK " + jdkVersion);
    }
}
