package clientSide;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static Scanner userInputScanner; // Single scanner for user input
    private static Scanner serverScanner;    // Single scanner for server input
    private static PrintWriter printWriter;
    private static boolean isWhite;

    private static AtomicBoolean isConnected = new AtomicBoolean(false);
    private final static int HEARTBEAT = 5000;

    static Socket gameSocket;
    static Socket heartbeatSocket;
    static String role;

    public static void main(String[] args) throws IOException {
        gameSocket = new Socket("localhost", 10000);

        try{
            Scanner scanner = new Scanner(gameSocket.getInputStream());
            String message = scanner.nextLine(); // should get heartbeat socket port
            int heartbeatPort = Integer.parseInt(message.split(":")[1]);
            heartbeatSocket = new Socket("localhost",heartbeatPort);
            isConnected.set(true);
        }
        catch (Exception e){
            System.out.println("error during connecting to server");
            return;
        }

        // heartbeat handling method
        handleHeartbeat();

        // Initialize streams once
        OutputStream outputStream = gameSocket.getOutputStream();
        printWriter = new PrintWriter(outputStream, true);
        InputStream inputStream = gameSocket.getInputStream();
        serverScanner = new Scanner(inputStream);
        userInputScanner = new Scanner(System.in); // single scanner for all user input

        role = assignRole();
        isConnected.set(true);


        if (role.equals("player")) {
            String color = serverScanner.nextLine();
            if(color.equals("white")){
                isWhite = true;
                System.out.println("I am white");
            }
            else {
                isWhite = false;
                System.out.println("I am black");
            }
            clientPlayer();
        } else {
            clientSpectator();
        }

        // clean up resources
        userInputScanner.close();
        serverScanner.close();
        gameSocket.close();
    }

    private static void handleHeartbeat() throws IOException {
        OutputStream heartbeatOut = heartbeatSocket.getOutputStream();
        PrintWriter heartbeatPrinter = new PrintWriter(heartbeatOut, true);

        System.out.println("connect to heartbeat");

        // start the heartbeat
        Thread.startVirtualThread(() -> heartbeatHandler(heartbeatPrinter));
        System.out.println("heartbeat started");
    }

    private static void heartbeatHandler(PrintWriter heartbeatWriter) {
        while (isConnected.get()){
                heartbeatWriter.println("HEARTBEAT");
            try {
                Thread.sleep(HEARTBEAT);
            } catch (InterruptedException e) {
                System.out.println("Heartbeat thread stopping...");
                isConnected.set(false);
                break;
            }
            catch (Exception e){
                System.out.println("Heartbeat error: " + e.getMessage());
                isConnected.set(false);
                break;
            }
        }
    }

    private static String assignRole() {
        System.out.println("Choose your role: player/spectator");

        while (true) {
            String input = userInputScanner.nextLine().trim();
            printWriter.println(input);

            // read server responses
            String informativeMessage = serverScanner.nextLine();
            System.out.println(informativeMessage);

            String acknowledgement = serverScanner.nextLine();

            if (acknowledgement.equals("ok")) {
                System.out.println(input + " connected - on client side");
                return input;
            }
            // if not ok, continue the loop to try again
        }
    }

    private static void clientPlayer() {
        System.out.println("You are now a player. Waiting for game to start...");

        while (isConnected.get()) {
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
                isConnected.set(false);
                break;
            }
        }
    }

    private static void clientSpectator() {
        System.out.println("You are now a spectator. Watching the game...");

        try {
            while (isConnected.get()) {
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
            isConnected.set(false);
        }
    }
}