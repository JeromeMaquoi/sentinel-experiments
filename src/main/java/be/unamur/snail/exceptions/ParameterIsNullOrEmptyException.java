package be.unamur.snail.exceptions;

public class ParameterIsNullOrEmptyException extends NullPointerException{
    public ParameterIsNullOrEmptyException(String param) {
        super(String.format("%s is null or empty", param));
    }
}
