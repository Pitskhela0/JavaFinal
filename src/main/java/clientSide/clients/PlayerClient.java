package clientSide.clients;

import clientSide.utils.ServerConnector;

import java.io.PrintWriter;
import java.util.Scanner;

public class PlayerClient {
    private boolean isWhite;
    private ServerConnector serverConnector;
    private Scanner serverScanner;
    private PrintWriter printWriter;
    private Scanner userInputScanner;
    public PlayerClient(boolean isWhite, ServerConnector serverConnector, Scanner serverScanner, PrintWriter printWriter,
                        Scanner userInputScanner){
        this.isWhite = isWhite;
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;
        this.printWriter = printWriter;
        this.userInputScanner = userInputScanner;
    }
    public void runPlayer() {
        System.out.println("You are now a player. Waiting for game to start...");

        while (serverConnector.getIsConnected().get()) {
            try {
                String messageFromServer = serverScanner.nextLine();

                if (messageFromServer.equals("update")) {
                    // This is a board update from opponent's move
                    String moveUpdate = serverScanner.nextLine();
                    if(moveUpdate.equals("resign")){
                        System.out.println("Game ended");
                        break;
                    }
                    if(moveUpdate.equals("error")){
                        System.out.println("Game ended - error from client");
                        break;
                    }
                    System.out.println("Opponent's move: " + moveUpdate);

                } else if (messageFromServer.equals("enter your move")) {
                    // Server is asking for our move
                    System.out.println("Your turn! " + messageFromServer);
                    String userMove = userInputScanner.nextLine().trim();
                    printWriter.println(userMove);
                } else {
                    // Other server messages
                    System.out.println("Server: " + messageFromServer);
                }
            } catch (Exception e) {
                System.out.println("Connection lost or error occurred: " + e.getMessage());
                serverConnector.getIsConnected().set(false);
                closeAllResources();
                break;
            }
        }
    }
    private void closeAllResources(){
        // close every given resources

    }
}
