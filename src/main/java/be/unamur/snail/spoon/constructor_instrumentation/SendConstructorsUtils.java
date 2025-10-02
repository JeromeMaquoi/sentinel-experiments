package be.unamur.snail.spoon.constructor_instrumentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashSet;
import java.util.List;

public class SendConstructorsUtils {
    private ConstructorContext constructorContext;
    private final StackTraceHelper stackTraceHelper;
    private final ConstructorContextSender sender;

    public SendConstructorsUtils(ConstructorContextSender sender) {
        this.constructorContext = new ConstructorContext();
        this.stackTraceHelper = new StackTraceHelper(new DefaultStackTraceProvider());
        this.sender = sender;
    }

    public SendConstructorsUtils(StackTraceHelper stackTraceHelper, ConstructorContextSender sender) {
        this.constructorContext = new ConstructorContext();
        this.stackTraceHelper = stackTraceHelper;
        this.sender = sender;
    }

    public ConstructorContext getConstructorContextForTests() {
        return constructorContext;
    }

    /**
     * Initialize a new ConstructorContext object
     * @param fileName file where the constructor is
     * @param className class where the constructor is
     * @param methodName method/constructor name
     * @param parameters a list of parameters of the constructor
     */
    public void initConstructorContext(String fileName, String className, String methodName, List<String> parameters) {
        constructorContext = constructorContext.withFileName(fileName).withClassName(className).withMethodName(methodName).withParameters(parameters).withAttributes(new HashSet<>());
        System.out.println("Constructor context is: " + constructorContext);
    }

    /**
     * Add an attribute to an already initialized ConstructorContext
     * @param attributeName name of the attribute
     * @param attributeType type of the attribute
     * @param actualObject effective type of the attribute
     * @param rightHandSideExpressionType type of the right hand side expression of the attribute
     * @throws IllegalStateException if the constructor context is not initialized
     */
    public void addAttribute(String attributeName, String attributeType, Object actualObject, String rightHandSideExpressionType) {
        if (constructorContext == null || constructorContext.isEmpty()) {
            throw new IllegalStateException("ConstructorContext is not initialized");
        }

        String actualType = actualObject != null ? actualObject.getClass().getName() : "null";
        AttributeContext attributeContext = new AttributeContext(attributeName, attributeType, actualType, rightHandSideExpressionType);
        constructorContext.addAttribute(attributeContext);
        System.out.println("Added attribute " + attributeName + " to context");
    }

    /**
     * Get the stacktrace of this at a t time, and puts it in the
     * current constructor context
     */
    public void getStackTrace() {
        if (constructorContext == null || constructorContext.isEmpty()) {
            throw new IllegalStateException("ConstructorContext is not initialized");
        }
        List<StackTraceElement> stackTrace = stackTraceHelper.getFilteredStackTrace();
        constructorContext = constructorContext.withStackTrace(stackTrace);
    }

    public void send() {
        System.out.println("Sending instance to the database");
        if (sender == null) {
            throw new IllegalStateException("Sender is not initialized");
        }
        sender.send(constructorContext);
    }

    public String serializeConstructorContext(ConstructorContext context) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }
}
