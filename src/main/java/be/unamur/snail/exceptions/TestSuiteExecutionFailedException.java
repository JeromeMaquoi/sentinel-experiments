package be.unamur.snail.exceptions;

public class TestSuiteExecutionFailedException extends RuntimeException {
    public TestSuiteExecutionFailedException() {
        super("Project test execution failed");
    }
}
