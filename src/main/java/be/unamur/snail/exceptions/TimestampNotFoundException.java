package be.unamur.snail.exceptions;

public class TimestampNotFoundException extends IllegalArgumentException {
    public TimestampNotFoundException(String fileName) {
        super("No timestamp found in file name: " + fileName);
    }
}
