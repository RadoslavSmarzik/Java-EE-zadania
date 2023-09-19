package ee.connect4;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class Game {
    public static int NROWS = 5;
    public static int NCOLS = 7;

    int id;
    Session players[];
    int board[][];
    boolean gameOver;
    int won;
    int onMove;
    int numMoves;
    int lastStarted;

    private static final int dir[][] = {{-1, 0}, {0, -1}, {-1, -1}, {-1, 1}};

    public Game() {
        board = new int[NROWS][NCOLS];
        players = new Session[2];
        lastStarted = 1;
        restart();
    }

    public void restart() {
        for (int i = 0; i < NROWS; i++)
            for (int j = 0; j < NCOLS; j++)
                board[i][j] = -1;
        gameOver = false;
        won = -2;
        onMove = 1 - lastStarted;
        lastStarted = onMove;
        numMoves = 0;
    }

    public void makeMove(int r, int c) {
        board[r][c] = onMove;
        numMoves++;
        verifyGameOver(r, c);
        onMove = 1 - onMove;
        try {
            players[onMove].getBasicRemote().sendText(id + ";" + r + ";" + c + ";" + won);
            if (won != -2)
                players[1 - onMove].getBasicRemote().sendText(id + ";-1;-1;" + won);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void verifyGameOver(int r, int c) {
        for (int d = 0; d < 4; d++) {
            int sr = r;
            int sc = c;
            while ((sr + dir[d][0] >= 0) && (sc + dir[d][1] >= 0) && (sc + dir[d][1] < NCOLS))
                if (board[sr][sc] == board[sr + dir[d][0]][sc + dir[d][1]]) {
                    sr += dir[d][0];
                    sc += dir[d][1];
                } else break;
            int er = r;
            int ec = c;
            while ((er - dir[d][0] < NROWS) && (ec - dir[d][1] < NCOLS) && (ec - dir[d][1] >= 0))
                if (board[er][ec] == board[er - dir[d][0]][ec - dir[d][1]]) {
                    er -= dir[d][0];
                    ec -= dir[d][1];
                } else break;
            if ((er - sr >= 3) || (ec - sc >= 3)) {
                gameOver = true;
                won = onMove;
                return;
            }
        }
        if (numMoves == NROWS * NCOLS) {
            gameOver = true;
            won = -1;
            return;
        }
    }
}










@Named
@ApplicationScoped
public class Connect4
{
    private int nextID = 1;
    Map<Integer, Game> games = new HashMap<>();
    Game waitingGame = null;

    public synchronized void newPlayer(Session playerSession)
    {
        if(waitingGame == null)
        {
            Game g = new Game();
            g.id = nextID++;
            g.players[0] = playerSession;
            games.put(g.id, g);
            waitingGame = g;
            return;
        }
        waitingGame.players[1] = playerSession;
        int id = waitingGame.id;

        try {
            waitingGame.players[0].getBasicRemote().sendText(id + ";-1;-1;-2");
        } catch (IOException e) { System.out.println(e.getMessage()); }
        waitingGame = null;
    }

    public synchronized  void playerLeft(Session s)
    {
        int toRemove = -1;
        for (Game g: games.values())
        {
            if (g.players[0] == s)
            {
                g.players[0] = null;
                if (g.players[1] == null) toRemove = g.id;
            }
            else if (g.players[1] == s)
            {
                g.players = null;
                if (g.players[0] == null) toRemove = g.id;
            }
        }
        if (toRemove != -1) games.remove(toRemove);
    }

    public void makeMove(int id, int r, int c)
    {
        Game g = games.get(id);
        if (g != null)
        {
            g.makeMove(r, c);
            if (g.gameOver) g.restart();
        }
    }
}

