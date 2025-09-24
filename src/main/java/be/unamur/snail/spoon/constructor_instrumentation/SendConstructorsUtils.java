package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.HashSet;
import java.util.List;

public class SendConstructorsUtils {
    private ConstructorContext constructorContext;

    public SendConstructorsUtils() {
        this.constructorContext = new ConstructorContext();
    }
    
    public void initConstructorContext(String fileName, String className, String methodName, List<String> parameters) {
        constructorContext = constructorContext.withFileName(fileName).withClassName(className).withMethodName(methodName).withParameters(parameters).withAttributes(new HashSet<>());
        System.out.println("Constructor context is: " + constructorContext);
    }

    public void addAttribute(String attributeName, String attributeType, Object actualObject, String rightHandSideExpressionType) {
        if (constructorContext == null || constructorContext.isEmpty()) {
            throw new IllegalStateException("ConstructorContext is not initialized");
        }

        String actualType = actualObject != null ? actualObject.getClass().getName() : "null";
        AttributeContext attributeContext = new AttributeContext(attributeName, attributeType, actualType, rightHandSideExpressionType);
        constructorContext.addAttribute(attributeContext);
        System.out.println("Added attribute " + attributeName + " to context");
    }

    public void getStackTrace(Object object) {
        System.out.println("Getting stack trace");
    }

    public void send() {
        System.out.println("Sending instance to the database");
    }

    public ConstructorContext getConstructorContextForTests() {
        return constructorContext;
    }
}
