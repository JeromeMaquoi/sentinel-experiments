package be.unamur.snail.spoon.constructor_instrumentation;

public class HttpConstructorContextSender implements ConstructorContextSender {
    private final HttpClientService client;
    private final String apiURL;

    public HttpConstructorContextSender(HttpClientService client, String apiURL) {
        this.client = client;
        this.apiURL = apiURL;
    }

    @Override
    public void send(ConstructorContext context) {
        SendConstructorsUtils utils = new SendConstructorsUtils(null);
        String json = utils.serializeConstructorContext(context);
        try {
            client.post(apiURL, json);
        } catch (Exception e) {
            throw new ConstructorContextSendFailedException(e);
        }
    }
}
