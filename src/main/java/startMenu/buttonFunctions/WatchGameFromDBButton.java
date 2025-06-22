package startMenu.buttonFunctions;

import clientSide.Client;
import serverSide.Server;
import shared.ChessMove;
import startMenu.ClientConnection;

import javax.swing.*;
import java.util.List;

public class WatchGameFromDBButton {
    private Thread currentServerThread;
    private Server currentServer;
    private ClientConnection clientConnection;
    private int GAME_ID;

    public int getGAME_ID() {
        return GAME_ID;
    }

    public WatchGameFromDBButton(ClientConnection clientConnection){
        this.clientConnection = clientConnection;
        this.GAME_ID = gameIDGenerator();
    }

    private int gameIDGenerator(){
        // gives every game different game id
        return 0;
    }

    public void startWatching(){
        // Stop any existing server first

        // Hide the menu window
        clientConnection.setVisible(false);

//        List<ChessMove> moveList = retreiveFromDB();
//
//
//        for (ChessMove chessMove : moveList) {
//            updateBoard(chessMove);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        clientConnection.setVisible(true);
    }

    private List<ChessMove> retreiveFromDB() {
        return null;
    }
}
