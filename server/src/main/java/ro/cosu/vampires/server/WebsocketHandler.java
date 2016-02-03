package ro.cosu.vampires.server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebSocket
public class WebsocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static Map<Session, String> clients = new HashMap<>();

    public static void broadcastMessage(String sender, String message) {

        LOG.info("message from {} : {}", sender, message);
        clients.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(message);
            } catch (Exception e) {
                LOG.error("{}", e);
            }
        });
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String clientId = UUID.randomUUID().toString();
        clients.put(user, clientId);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        clients.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        //
    }

}
