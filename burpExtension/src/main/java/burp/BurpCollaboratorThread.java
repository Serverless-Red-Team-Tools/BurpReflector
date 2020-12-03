package burp;

import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import java.lang.Thread;


public class BurpCollaboratorThread extends Thread {


    private URI uri;
    private WebSocketConnectionCallback webSocketConnectionCallback;
    private Test test;

    public BurpCollaboratorThread(URI uri, WebSocketConnectionCallback webSocketConnectionCallback){

        this.uri = uri;
        this.webSocketConnectionCallback = webSocketConnectionCallback;
    }

    @Override
    public void run(){
        System.out.println("thread");
        this.test = new Test(this.uri, this.webSocketConnectionCallback);
        test.connect();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        test.close();

    }
}
