package be.unamur.snail.spoon.constructor_instrumentation;

import java.util.ArrayList;
import java.util.List;

public class SendConstructorsUtils {
    private ConstructorContext constructorContext;
    private final StackTraceHelper stackTraceHelper;
    private final ConstructorContextSender sender; // Only used in tests
    private static volatile  ConstructorEventDispatcher dispatcher;
    private static final ThreadLocal<SendConstructorsUtils> LOCAL = new ThreadLocal<SendConstructorsUtils>() {
        @Override
        protected SendConstructorsUtils initialValue() {
            return new SendConstructorsUtils();
        }
    };

    public static SendConstructorsUtils getInstance() {
        return LOCAL.get();
    }

    public SendConstructorsUtils() {
        this.constructorContext = new ConstructorContext();
        this.stackTraceHelper = new StackTraceHelper(new DefaultStackTraceProvider());
        this.sender = null;
        initDispatcher();
    }

    // Constructor for tests
    public SendConstructorsUtils(StackTraceHelper stackTraceHelper, ConstructorContextSender sender) {
        this.constructorContext = new ConstructorContext();
        this.stackTraceHelper = stackTraceHelper;
        this.sender = sender;
//        initDispatcher();
    }

    private static void initDispatcher() {
        if (dispatcher == null) {
            synchronized (SendConstructorsUtils.class) {
                if (dispatcher == null) {
                    String apiURL = System.getProperty("apiUrl", System.getenv("API_URL"));
                    if (apiURL == null || apiURL.isEmpty()) {
                        throw new IllegalArgumentException("apiUrl not set");
                    }
                    dispatcher = ConstructorEventDispatcher.getInstance(apiURL);
                }
            }
        }
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
        constructorContext = constructorContext.withFileName(fileName).withClassName(className).withMethodName(methodName).withParameters(parameters).withAttributes(new ArrayList<>());
    }

    public void resetConstructorContextForTests() {
        constructorContext = null;
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
//        System.out.println("Added attribute " + attributeName + " to context");
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
//        System.out.println("Stack trace: " + stackTrace);
        constructorContext = constructorContext.withStackTrace(stackTrace);
    }

    /**
     * Send the constructor data into the database
     */
    public void send() {
        if (!constructorContext.isComplete()) {
            throw new ConstructorContextNotCompletedException();
        }
        if (sender != null) {
            sender.send(constructorContext);
        } else {
            if (dispatcher == null) {
                initDispatcher();
            }
//            System.out.println("Sending instance to the database: " + constructorContext);
            dispatcher.submit(constructorContext);
        }
    }
}
