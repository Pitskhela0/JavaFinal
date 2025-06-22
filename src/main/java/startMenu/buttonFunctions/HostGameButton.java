package startMenu.buttonFunctions;

import clientSide.Client;
import serverSide.Server;
import startMenu.ClientConnection;

import javax.swing.*;

public class HostGameButton {
    private Thread currentServerThread;
    private Server currentServer;
    private ClientConnection clientConnection;
    private int GAME_ID;

    public int getGAME_ID() {
        return GAME_ID;
    }

    public HostGameButton(ClientConnection clientConnection){
        this.clientConnection = clientConnection;
        this.GAME_ID = gameIDGenerator();
    }

    private int gameIDGenerator(){
        return 0;
    }

    public void hostGame(){
        // Stop any existing server first
        clientConnection.stopCurrentServer();

        // Hide the menu window
        clientConnection.setVisible(false);

        // Start server in background thread

        currentServer = new Server(GAME_ID);


        currentServerThread = new Thread(() -> {
            try {
                System.out.println("Starting new server thread...");
                currentServer.start();
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                SwingUtilities.invokeLater(() -> {
                    clientConnection.showStartMenu();
                });
            }
        });
        currentServerThread.setName("Chess-Server-Thread");
        currentServerThread.setDaemon(true);
        currentServerThread.start();

        // Wait longer for server to start and add verification
        try {
            Thread.sleep(3000); // Increased wait time

            // Verify server is actually running by checking thread state
            if (!currentServerThread.isAlive()) {
                throw new RuntimeException("Server thread died during startup");
            }

            System.out.println("Server should be ready, starting client...");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Start client in background thread (not on EDT)
        Thread whitePlayer = new Thread(() -> {
            try {
                // Add small additional delay
                Thread.sleep(500);
                Client client = new Client(currentServer.getPort(), "player", clientConnection);
                client.start();
            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
                e.printStackTrace();

                // If client fails, show start menu again
                SwingUtilities.invokeLater(() -> {
                    clientConnection.showStartMenu();
                });
            }
        });
        whitePlayer.setName("Chess-Client-Thread");
        whitePlayer.start();
    }
}
