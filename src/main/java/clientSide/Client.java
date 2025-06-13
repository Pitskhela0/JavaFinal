package clientSide;

import clientSide.client.PlayerClient;
import clientSide.client.SpectatorClient;
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
        gameSocket = new Socket("localhost", 10000);

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
                System.out.println("I am white");
                PlayerClient whitePlayer = new PlayerClient(true, serverConnector, serverScanner, printWriter, userInputScanner);
                whitePlayer.runPlayer();
            }
            else {
                System.out.println("I am black");
                PlayerClient whitePlayer = new PlayerClient(false, serverConnector, serverScanner, printWriter, userInputScanner);
                whitePlayer.runPlayer();
            }
        } else {
            SpectatorClient spectator = new SpectatorClient(serverConnector, serverScanner);
            spectator.clientSpectator();
        }

        // clean up resources
        userInputScanner.close();
        serverScanner.close();
        gameSocket.close();
    }

    private static String assignRole(){
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
}