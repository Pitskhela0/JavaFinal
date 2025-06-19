package clientSide;

import clientSide.clients.PlayerClient;
import clientSide.clients.SpectatorClient;
import clientSide.utils.ServerConnector;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Scanner userInputScanner; // Single scanner for user input
    private static Scanner serverScanner;    // Single scanner for server input
    private static PrintWriter printWriter;
    private static ServerConnector serverConnector;
    private static Socket gameSocket;
    private static String role;

    public static void main(String[] args) throws IOException {
        try{
            System.out.println("Connecting to chess server...");
            gameSocket = new Socket("localhost", 10000);
            System.out.println("Connected to server successfully!");
        }
        catch (Exception e){
            System.out.println("Cannot connect to server. Make sure the server is running on localhost:10000");
            return;
        }

        try {
            serverConnector = new ServerConnector(gameSocket);

            // heartbeat handling method
            serverConnector.handleHeartbeat();
            // listen to the server using second socket, not for actual game, for other processes
            serverConnector.serverConnectionListener();

            // Initialize streams once
            OutputStream outputStream = gameSocket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);
            InputStream inputStream = gameSocket.getInputStream();
            serverScanner = new Scanner(inputStream);
            userInputScanner = new Scanner(System.in); // single scanner for all user input

            // assigning the role
            role = assignRole();

            if (role.equals("player")) {
                String color = serverScanner.nextLine();
                if(color.equals("white")){
                    System.out.println("You are assigned as White player");
                    PlayerClient whitePlayer = new PlayerClient(true, serverConnector, serverScanner, printWriter, userInputScanner);
                    whitePlayer.runPlayer();
                }
                else {
                    System.out.println("You are assigned as Black player");
                    PlayerClient blackPlayer = new PlayerClient(false, serverConnector, serverScanner, printWriter, userInputScanner);
                    blackPlayer.runPlayer();
                }
            } else if(role.equals("spectator")){
                SpectatorClient spectator = new SpectatorClient(serverConnector, serverScanner);
                spectator.clientSpectator();
            }
            else {
                System.out.println("Server connection failed or server is closed");
            }

        } catch (Exception e) {
            System.out.println("Error during game: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // clean up resources
            cleanup();
        }
    }

    private static String assignRole(){
        System.out.println("Choose your role:");
        System.out.println("- Type 'player' to play the game");
        System.out.println("- Type 'spectator' to watch the game");
        System.out.print("Your choice: ");

        while (true) {
            String input = userInputScanner.nextLine().trim().toLowerCase();
            printWriter.println(input);

            // read server responses
            String informativeMessage;
            try{
                informativeMessage = serverScanner.nextLine();
                System.out.println("Server: " + informativeMessage);
            }
            catch (Exception e){
                System.out.println("You cannot connect to server, it is closed");
                break;
            }

            String acknowledgement = serverScanner.nextLine();

            if (acknowledgement.equals("ok")) {
                System.out.println("Successfully connected as " + input);
                return input;
            } else {
                System.out.print("Please try again (player/spectator): ");
            }
            // if not ok, continue the loop to try again
        }
        return "";
    }

    private static void cleanup() {
        try {
            if (userInputScanner != null) {
                userInputScanner.close();
            }
            if (serverScanner != null) {
                serverScanner.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (gameSocket != null && !gameSocket.isClosed()) {
                gameSocket.close();
            }
            if (serverConnector != null) {
                // Close serverConnector if it has cleanup methods
            }
        } catch (Exception e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }

        System.out.println("Client disconnected. Thank you for playing!");
    }
}