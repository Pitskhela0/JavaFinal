package serverSide;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler {
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    boolean isPlayer = false;
    boolean isWhitePlayer = false;
    PrintWriter printWriter;
    Scanner scanner;
    boolean assigned = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        printWriter = new PrintWriter(outputStream, true);
        scanner = new Scanner(inputStream);
    }

    public void handleClient() {
        try {
            // role assignment
            assignRole();
        } catch (Exception e) {
            System.out.println("Error handling client: " + e.getMessage());
            cleanup();
        }
    }

    public static void broadcastPlayer(ClientHandler client, Move move) {
        try {
            client.printWriter.println("update");
            client.printWriter.println(move.getMove());
        } catch (Exception e) {
            System.out.println("Error broadcasting to player: " + e.getMessage());
        }
    }

    private void assignRole() {
        while (!assigned && socket.isConnected()) {
            try {
                System.out.println("Current players: " + Server.players.size());

                if (!scanner.hasNextLine()) {
                    break; // Client disconnected
                }

                String userInput = scanner.nextLine().trim();
                System.out.println("Role request: " + userInput);

                if (userInput.equals("player")) {
                    if (Server.players.size() >= 2) {
                        // informative message
                        printWriter.println("There are already 2 players");
                        // acknowledgement
                        printWriter.println("not ok");
                    } else {
                        // informative message
                        printWriter.println("You successfully connected to server as Player");
                        // acknowledgement
                        printWriter.println("ok");
                        assigned = true;
                        isPlayer = true;
                        System.out.println("Player has connected");
                    }
                } else if (userInput.equals("spectator")) {
                    assigned = true;
                    System.out.println("Spectator has connected");
                    // informative message
                    printWriter.println("You successfully connected to server as Spectator");
                    // acknowledgement
                    printWriter.println("ok");

                    if(Server.gameStarted){
                        // mid game spectator
                        sendFullBoard(Server.board);
                    }
                } else {
                    // informative message
                    printWriter.println("Invalid role. Please choose 'player' or 'spectator'");
                    // acknowledgement
                    printWriter.println("not ok");
                }
            } catch (Exception e) {
                System.out.println("Client disconnected during role assignment");
                break;
            }
        }

        if (assigned) {
            if (isPlayer) {
                synchronized (Server.players) {
                    Server.players.add(this);
                    System.out.println("Total players: " + Server.players.size());

                    if (Server.players.size() == 1) {
                        isWhitePlayer = true;
                        System.out.println("Assigned as White player");
                    } else {
                        isWhitePlayer = false;
                        System.out.println("Assigned as Black player");
                    }
                }

                // Check if game should start
                if (!Server.gameStarted && Server.players.size() == 2) {
                    Server.gameStarted = true;
                    new Thread(Server::startGame).start();
                }
            } else {
                synchronized (Server.spectators) {
                    Server.spectators.add(this);
                    System.out.println("Total spectators: " + Server.spectators.size());
                }
            }
        } else {
            cleanup();
        }
    }

    // if client is spectator, use for broadcasting
    public void sendUpdate(Move move) {
        try {
            printWriter.println(move.getMove());
        } catch (Exception e) {
            System.out.println("Error sending update to spectator: " + e.getMessage());
        }
    }

    public Move handleMove(String color) {
        try {
            printWriter.println("enter your move");

            if (!scanner.hasNextLine()) {
                return new Move("resign"); // Player disconnected
            }

            String input = scanner.nextLine().trim();
            System.out.println("Move from " + color + " player: " + input);
            return new Move(input);
        } catch (Exception e) {
            System.out.println("Error getting move from " + color + " player: " + e.getMessage());
            return new Move("error");
        }
    }

    private void cleanup() {
        try {
            if (scanner != null) scanner.close();
            if (printWriter != null) printWriter.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void sendFullBoard(Board board) {
        printWriter.println("MID_GAME");
    }
}