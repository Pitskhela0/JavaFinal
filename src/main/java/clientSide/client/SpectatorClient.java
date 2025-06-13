package clientSide.client;

import clientSide.utils.ServerConnector;

import java.util.Scanner;

public class SpectatorClient {

    private ServerConnector serverConnector;
    private Scanner serverScanner;
    public SpectatorClient(ServerConnector serverConnector, Scanner serverScanner){
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;
    }

    public void clientSpectator() {
        System.out.println("You are now a spectator. Watching the game...");

        try {
            while (serverConnector.getIsConnected().get()) {
                String messageFromServer = serverScanner.nextLine();
                if(messageFromServer.equals("MID_GAME")){
                    System.out.println("MID_GAME update, board was sent");
                }
                else {
                    System.out.println("Game update: " + messageFromServer);
                }
            }
        } catch (Exception e) {
            System.out.println("Connection lost or error occurred: " + e.getMessage());
            serverConnector.getIsConnected().set(false);
            closeAllResources();
        }
    }
    private void closeAllResources(){
        // close all given resources
    }
}
