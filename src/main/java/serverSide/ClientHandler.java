package serverSide;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Scanner;

public class ClientHandler {
    private Socket moveSocket;

    public Socket getMoveSocket() {
        return moveSocket;
    }

    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isPlayer = false;
    private boolean isWhitePlayer = false;
    private PrintWriter printWriter;
    private Scanner scanner;
    private boolean assigned = false;
    private Socket heartbeatSocket;
    private boolean isAlive = true;

    public  boolean getIsAlive(){
        return isAlive;
    }
    private Scanner hearbeatScanner;

    public ClientHandler(Socket socket, Socket heartbeatSocket) {
        InputStream inputStream1;
        this.heartbeatSocket = heartbeatSocket;
        this.moveSocket = socket;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            inputStream1 = new BufferedInputStream(heartbeatSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        printWriter = new PrintWriter(outputStream, true);
        scanner = new Scanner(inputStream);

        hearbeatScanner = new Scanner(inputStream1);
        Thread.startVirtualThread(this::handleHeartbeat);
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

    private void handleHeartbeat() {

        boolean clientOut = false;

        long lastHeartbeat = System.currentTimeMillis();

        while (true){
            // check if waiting is too long here
            while (!hearbeatScanner.hasNextLine()){
                if(System.currentTimeMillis() - lastHeartbeat > 12_000){
                    System.out.println("wait time is out, good bye client");
                    clientOut = true;
                    break;
                }
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    throw new IndexOutOfBoundsException();
                }
            }
            if(clientOut){
                break;
            }
            String heartbeat = hearbeatScanner.nextLine();
            System.out.println(heartbeat);

            lastHeartbeat = System.currentTimeMillis();

        }
        System.out.println("heartbeat very out");
        isAlive = false;

        if(isPlayer){
            Server.endGame();

        }
        else {
            Server.kickoutSpectator();
        }

//        handleClosingEverything();
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
        while (!assigned && moveSocket.isConnected()) {
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
                        printWriter.println("white");
                        System.out.println("Assigned as White player");
                    } else {
                        isWhitePlayer = false;
                        printWriter.println("black");
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

    public Move handleMessagePlayer(String color) {
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
            if (moveSocket != null && !moveSocket.isClosed()) moveSocket.close();
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void sendFullBoard(Board board) {
        printWriter.println("MID_GAME");
    }

    public void close() {
        // send ack to client to close connection with game server via heartbeat socket
        try {
            OutputStream outputStream1 = new BufferedOutputStream(heartbeatSocket.getOutputStream());
            PrintWriter writer = new PrintWriter(outputStream1);
            writer.println("GAME_END");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}