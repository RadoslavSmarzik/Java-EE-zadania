package ee.connect4;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;



@ServerEndpoint("/game")
public class Communivate {

    @Inject
    Connect4 app;

    @OnMessage
    public void processMove(String message, Session session) {
        String[] msg = message.split(";");
        int id = Integer.parseInt(msg[0]);
        int r = Integer.parseInt(msg[1]);
        int c = Integer.parseInt(msg[2]);
        app.makeMove(id, r, c);
    }

    @OnOpen
    public void open(Session session, EndpointConfig conf)
    {
        app.newPlayer(session);
    }

    @OnClose
    public  void close(Session session, CloseReason reason)
    {
        app.playerLeft(session);
    }
}

