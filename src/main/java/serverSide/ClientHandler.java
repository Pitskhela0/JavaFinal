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
                if(System.currentTimeMillis() - lastHeartbeat > 1000){
                    System.out.println("wait time is out, good bye client");
                    clientOut = true;
                    break;
                }
                try{
                    Thread.sleep(200);
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
                System.out.println("Current players: " + Server.getPlayers().size());

                if (!scanner.hasNextLine()) {
                    break; // Client disconnected
                }

                String userInput = scanner.nextLine().trim();
                System.out.println("Role request: " + userInput);

                if (userInput.equals("player")) {
                    if (Server.getPlayers().size() >= 2) {
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

                    if(Server.isGameStarted()){
                        // mid game spectator
                        sendFullBoard(Server.getBoard());
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
                synchronized (Server.getPlayers()) {
                    Server.getPlayers().add(this);
                    System.out.println("Total players: " + Server.getPlayers().size());

                    if (Server.getPlayers().size() == 1) {
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
                if (!Server.isGameStarted() && Server.getPlayers().size() == 2) {
                    Server.setGameStarted(true);
                    new Thread(Server::startGame).start();
                }
            } else {
                synchronized (Server.getSpectators()) {
                    Server.getSpectators().add(this);
                    System.out.println("Total spectators: " + Server.getSpectators().size());
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
                // Player disconnected - end the game immediately
                System.out.println(color + " player disconnected");
                Server.endGame();
                return new Move("resign");
            }

            String input = scanner.nextLine().trim();
            System.out.println("Move from " + color + " player: " + input);
            return new Move(input);
        } catch (Exception e) {
            System.out.println("Error getting move from " + color + " player: " + e.getMessage());
            Server.endGame(); // End game on any communication error
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
        try {
            // Mark as not alive first
            isAlive = false;

            if(isPlayer){
                if(isWhitePlayer){
                    System.out.println("game end was sent to white player");
                } else {
                    System.out.println("game end was sent to black player");
                }
            } else {
                System.out.println("game end was sent to spectator");
            }

            // Send GAME_END through heartbeat socket
            OutputStream outputStream1 = heartbeatSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream1, true);
            writer.println("GAME_END");
            writer.flush();

            // Send resign through main socket
            printWriter.println("update");
            printWriter.println("resign");
            printWriter.flush();

            // Close sockets after a brief delay to ensure messages are sent
            Thread.sleep(100);

            // Close resources
            if (heartbeatSocket != null && !heartbeatSocket.isClosed()) {
                heartbeatSocket.close();
            }
            if (moveSocket != null && !moveSocket.isClosed()) {
                moveSocket.close();
            }

        } catch (Exception e) {
            System.out.println("Error closing client handler: " + e.getMessage());
        }
    }
}