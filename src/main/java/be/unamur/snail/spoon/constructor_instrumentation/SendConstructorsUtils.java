package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.List;

public class SendConstructorsUtils {
    public SendConstructorsUtils() {}
    
    public void initConstructorContext(String fileName, String className, String methodName, List<String> parameters) {
        System.out.println("Initializing constructor context");
    }

    public void addAttribute(String attribute, String type, Object actualObject, String rightHandSideExpressionType) {
        System.out.println("Adding attribute " + attribute + " of type " + type + " to " + rightHandSideExpressionType);
    }

    public void getStackTrace(Object object) {
        System.out.println("Getting stack trace");
    }

    public void send() {
        System.out.println("Sending instance to the database");
    }
}
