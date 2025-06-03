package client;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSide {

    public static void main(String[] args) {
        try {
            // Connect to the server on localhost port 10002
            Socket socket = new Socket("localhost", 10002);

            // Set up input/output streams
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);

            InputStream inputStream = socket.getInputStream();
            Scanner serverScanner = new Scanner(inputStream);

            // Scanner for user input
            Scanner userInput = new Scanner(System.in);

            // Read the initial timestamp from server
            if (serverScanner.hasNextLine()) {
                String timestamp = serverScanner.nextLine();
                System.out.println("Server timestamp: " + timestamp);
            }

            System.out.println("Connected to server. Type 'bye' to exit.");

            boolean player;

            while (true){
                System.out.println("Choose your role: player/spectator");
                String role = userInput.nextLine();

                printWriter.println(role);

                String response = serverScanner.nextLine();
                System.out.println(response);
                if(response.equals("ok")){
                    if(role.equals("player")){
                        player = true;
                    }
                    else {
                        player = false;
                    }
                    break;
                }
            }

            if (player){
                printWriter.println("player");

                player(userInput, printWriter, serverScanner, socket);
            }
            else {
                printWriter.println("spectator");
                spectator(userInput, printWriter, serverScanner, socket);
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void player(Scanner userInput, PrintWriter printWriter, Scanner serverScanner, Socket socket) throws IOException {
        // Main communication loop
        while (true) {
            System.out.print("Enter move: ");
            String message = userInput.nextLine();

            // Send message to server
            printWriter.println(message);

            // If user types "bye", break the loop
            if (message.equals("bye")) {
                break;
            }

            // Read server response
            if (serverScanner.hasNextLine()) {
                String response = serverScanner.nextLine();
                System.out.println("Server response: " + response);
            }
        }

        // Clean up resources
        printWriter.close();
        serverScanner.close();
        userInput.close();
        socket.close();

        System.out.println("Connection closed.");
    }

    private static void spectator(Scanner userInput, PrintWriter printWriter, Scanner serverScanner, Socket socket) throws IOException {
        // Main communication loop
        while (true) {
            System.out.print("if you want to quit type bye");
            String message = userInput.nextLine();

            // Send message to server
            printWriter.println(message);

            // If user types "bye", break the loop
            if (message.equals("bye")) {
                break;
            }

            // Read server response
            if (serverScanner.hasNextLine()) {
                String response = serverScanner.nextLine();
                System.out.println("Server response: " + response);
            }
        }

        // Clean up resources
        printWriter.close();
        serverScanner.close();
        userInput.close();
        socket.close();

        System.out.println("Connection closed.");
    }
}