package clientSide;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Scanner userInputScanner; // Single scanner for user input
    private static Scanner serverScanner;    // Single scanner for server input
    private static PrintWriter printWriter;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 10000);

        // Initialize streams once
        OutputStream outputStream = socket.getOutputStream();
        printWriter = new PrintWriter(outputStream, true);
        InputStream inputStream = socket.getInputStream();
        serverScanner = new Scanner(inputStream);
        userInputScanner = new Scanner(System.in); // Single scanner for all user input

        String role = assignRole();

        if (role.equals("player")) {
            clientPlayer();
        } else {
            clientSpectator();
        }

        // Clean up resources
        userInputScanner.close();
        serverScanner.close();
        socket.close();
    }

    private static String assignRole() {
        System.out.println("Choose your role: player/spectator");

        while (true) {
            String input = userInputScanner.nextLine().trim();
            printWriter.println(input);

            // Read server responses
            String informativeMessage = serverScanner.nextLine();
            System.out.println(informativeMessage);

            String acknowledgement = serverScanner.nextLine();

            if (acknowledgement.equals("ok")) {
                System.out.println(input + " connected - on client side");
                return input;
            }
            // If not ok, continue the loop to try again
        }
    }

    private static void clientPlayer() {
        System.out.println("You are now a player. Waiting for game to start...");

        while (true) {
            try {
                String messageFromServer = serverScanner.nextLine();

                if (messageFromServer.equals("update")) {
                    // This is a board update from opponent's move
                    String moveUpdate = serverScanner.nextLine();
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
                break;
            }
        }
    }

    private static void clientSpectator() {
        System.out.println("You are now a spectator. Watching the game...");

        try {
            while (true) {
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
        }
    }
}