package startMenu.buttonFunctions;

import clientSide.Client;
import startMenu.ClientConnection;

import javax.swing.*;

public class SpectateGameButton {
    private ClientConnection clientConnection;
    private int GAME_PORT;
    public SpectateGameButton(ClientConnection clientConnection, JTextField gameIDText){
        this.clientConnection = clientConnection;
        this.GAME_PORT = findGamePort(gameIDText);
    }

    private int findGamePort(JTextField gameIDText) {
        return 0;
    }

    public void spectate(){
        // find server with entered gameID and connect to its port as client
        try{
            clientConnection.setVisible(false);

            Thread blackPlayer = new Thread(() ->{
                try {
                    Thread.sleep(500);
                    Client client = new Client(10000, "spectator", clientConnection);
                    client.start();
                } catch (Exception e) {
                    // If client fails, show start menu again
                    SwingUtilities.invokeLater(() -> {
                        clientConnection.showStartMenu();
                    });
                }
            });
            blackPlayer.setName("Chess-Client-Thread");
            blackPlayer.start();
        }
        catch (Exception e){
            System.out.println("unable to join game");
        }
    }
}
