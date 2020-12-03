package burp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class Test extends WebSocketClient{

    private WebSocketConnectionCallback websocketconnectioncallback;

    public Test(URI serverURI, WebSocketConnectionCallback websocketconnectioncallback) {
        super(serverURI);
        this.websocketconnectioncallback = websocketconnectioncallback;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
        this.websocketconnectioncallback.onOpen(handshakedata);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
        this.websocketconnectioncallback.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
        this.websocketconnectioncallback.onClose(code,reason,remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        this.websocketconnectioncallback.onError(ex);
    }




}