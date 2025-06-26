package startMenu.buttonFunctions;

import startMenu.ClientConnection;

import javax.swing.*;

public class WatchGameFromDBButton {
    private final ClientConnection clientConnection;

    public WatchGameFromDBButton(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void startWatching() {
        clientConnection.setVisible(false);
        new GameReviewWindow(clientConnection, clientConnection); // open new window and pass back-ref
    }

}
