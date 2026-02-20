package be.unamur.snail.spoon.constructor_instrumentation;

public class HttpConstructorContextSender implements ConstructorContextSender {
    private final HttpClientService client;
    private final String apiURL;
    private final ConstructorContextSerializer serializer;

    public HttpConstructorContextSender(String apiURL) {
        this(new HttpClientService(), apiURL, new ConstructorContextSerializer());
    }

    // For dependency injection and tests
    public HttpConstructorContextSender(HttpClientService client, String apiURL, ConstructorContextSerializer serializer) {
        this.client = client;
        this.apiURL = apiURL;
        this.serializer = serializer;
    }

    @Override
    public void send(ConstructorContext context) {
        String json = serializer.serialize(context);
        try {
            client.post(apiURL, json);
//            System.out.println("Context " + context.getMethodName() + " sent successfully");
        } catch (Exception e) {
            throw new ConstructorContextSendFailedException(e);
        }
    }
}
