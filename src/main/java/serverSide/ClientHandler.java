package serverSide;

import shared.GameState;
import shared.ChessMove;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ClientHandler {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private Socket moveSocket;

    public Socket getMoveSocket() {
        return moveSocket;
    }

    private boolean isPlayer = false;
    private boolean isWhitePlayer = false;
    private final PrintWriter printWriter;
    private final Scanner scanner;
    private boolean assigned = false;
    private final Socket heartbeatSocket;
    private boolean isAlive = true;

    public boolean getIsAlive(){
        return isAlive;
    }
    private final Scanner heartbeatScanner;

    public ClientHandler(Socket socket, Socket heartbeatSocket) {
        InputStream inputStream1;
        this.heartbeatSocket = heartbeatSocket;
        this.moveSocket = socket;
        InputStream inputStream;
        OutputStream outputStream;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            inputStream1 = new BufferedInputStream(heartbeatSocket.getInputStream());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ClientHandler streams", e);
            throw new RuntimeException(e);
        }
        printWriter = new PrintWriter(outputStream, true);
        scanner = new Scanner(inputStream);

        heartbeatScanner = new Scanner(inputStream1);
        Thread.startVirtualThread(this::handleHeartbeat);

        LOGGER.info("ClientHandler initialized for new connection");
    }

    public void handleClient() {
        try {
            // role assignment
            assignRole();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling client", e);
            System.out.println("Error handling client: " + e.getMessage());
            cleanup();
        }
    }

    private void handleHeartbeat() {
        LOGGER.info("Starting heartbeat monitoring");
        boolean clientOut = false;

        long lastHeartbeat = System.currentTimeMillis();

        while (true){
            // check if waiting is too long here
            while (!heartbeatScanner.hasNextLine()){
                if(System.currentTimeMillis() - lastHeartbeat > 1000){
                    System.out.println("wait time is out, good bye client");
                    LOGGER.warning("Heartbeat timeout - client disconnected");
                    clientOut = true;
                    break;
                }
                try{
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    LOGGER.log(Level.WARNING, "Heartbeat thread interrupted", e);
                    throw new IndexOutOfBoundsException();
                }
            }
            if(clientOut){
                break;
            }
            String heartbeat = heartbeatScanner.nextLine();
            System.out.println(heartbeat);
            LOGGER.fine("Received heartbeat: " + heartbeat);

            lastHeartbeat = System.currentTimeMillis();

        }
        System.out.println("heartbeat very out");
        LOGGER.info("Heartbeat monitoring ended");
        isAlive = false;

        if(isPlayer){
            Server.endGame();
        }
        else {
            Server.kickoutSpectator();
        }
    }

    private void assignRole() {
        LOGGER.info("Starting role assignment");
        while (!assigned && moveSocket.isConnected()) {
            try {
                System.out.println("Current players: " + Server.getPlayers().size());

                if (!scanner.hasNextLine()) {
                    break; // Client disconnected
                }

                String userInput = scanner.nextLine().trim();
                System.out.println("Role request: " + userInput);
                LOGGER.info("Role request: " + userInput);

                if (userInput.equals("player")) {
                    if (Server.getPlayers().size() >= 2) {
                        // informative message
                        printWriter.println("There are already 2 players");
                        // acknowledgement
                        printWriter.println("not ok");
                        printWriter.flush();
                    } else {
                        // informative message
                        printWriter.println("You successfully connected to server as Player");
                        // acknowledgement
                        printWriter.println("ok");
                        printWriter.flush();
                        assigned = true;
                        isPlayer = true;
                        System.out.println("Player has connected");
                        LOGGER.info("Player has connected");
                    }
                } else if (userInput.equals("spectator")) {
                    assigned = true;
                    System.out.println("Spectator has connected");
                    LOGGER.info("Spectator has connected");
                    // informative message
                    printWriter.println("You successfully connected to server as Spectator");
                    // acknowledgement
                    printWriter.println("ok");
                    printWriter.flush();

                    if(Server.isGameStarted()){
                        // mid game spectator - send current game state
                        GameState currentState = new GameState(Server.getBoard());
                        sendGameState(currentState);
                    }
                } else {
                    // informative message
                    printWriter.println("Invalid role. Please choose 'player' or 'spectator'");
                    // acknowledgement
                    printWriter.println("not ok");
                    printWriter.flush();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Client disconnected during role assignment", e);
                System.out.println("Client disconnected during role assignment");
                break;
            }
        }

        if (assigned) {
            if (isPlayer) {
                synchronized (Server.getPlayers()) {
                    Server.getPlayers().add(this);
                    System.out.println("Total players: " + Server.getPlayers().size());
                    LOGGER.info("Total players: " + Server.getPlayers().size());

                    if (Server.getPlayers().size() == 1) {
                        isWhitePlayer = true;
                        printWriter.println("white");
                        printWriter.flush();
                        System.out.println("Assigned as White player");
                        LOGGER.info("Assigned as White player");
                    } else {
                        isWhitePlayer = false;
                        printWriter.println("black");
                        printWriter.flush();
                        System.out.println("Assigned as Black player");
                        LOGGER.info("Assigned as Black player");
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
                    LOGGER.info("Total spectators: " + Server.getSpectators().size());
                }
            }
        } else {
            cleanup();
        }
    }

    // Send game state to client
    public void sendGameState(GameState gameState) {
        try {
            printWriter.println("GAME_STATE_UPDATE");
            printWriter.println(serializeGameState(gameState));
            printWriter.flush();
            LOGGER.fine("Game state sent to client");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending game state", e);
        }
    }

    // Request move from player and return ChessMove object
    public ChessMove requestMoveFromPlayer() {
        try {
            printWriter.println("REQUEST_MOVE");
            printWriter.flush();
            LOGGER.info("Move requested from player");

            if (!scanner.hasNextLine()) {
                LOGGER.warning("Player disconnected during move request");
                return null; // Player disconnected
            }

            String moveString = scanner.nextLine().trim();
            LOGGER.info("Received move string: " + moveString);

            ChessMove move = ChessMove.fromString(moveString);
            LOGGER.info("Parsed move: " + move);

            return move;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error requesting move from player", e);
            return ChessMove.error();
        }
    }

    // Send invalid move message to player
    public void sendInvalidMoveMessage() {
        try {
            printWriter.println("INVALID_MOVE");
            printWriter.println("Invalid move. Please try again.");
            printWriter.flush();
            LOGGER.info("Invalid move message sent to player");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending invalid move message", e);
        }
    }

    // Serialize GameState to string format for transmission
    private String serializeGameState(GameState gameState) {
        StringBuilder sb = new StringBuilder();

        // Serialize board state
        String[][] board = gameState.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null) {
                    sb.append(row).append(",").append(col).append(",").append(board[row][col]).append(";");
                }
            }
        }

        // Always add the separator, even if board is empty
        sb.append("|");

        // Serialize metadata
        sb.append("whiteTurn:").append(gameState.isWhiteTurn()).append(";");
        sb.append("whiteInCheck:").append(gameState.isWhiteInCheck()).append(";");
        sb.append("blackInCheck:").append(gameState.isBlackInCheck()).append(";");
        sb.append("gameOver:").append(gameState.isGameOver()).append(";");
        sb.append("winner:").append(gameState.getWinner() != null ? gameState.getWinner() : "none").append(";");
        sb.append("moveCount:").append(gameState.getMoveCount()).append(";");
        sb.append("lastMove:").append(gameState.getLastMove() != null ? gameState.getLastMove() : "none");

        String result = sb.toString();
        LOGGER.info("Serialized game state: " + result);
        return result;
    }

    private void cleanup() {
        LOGGER.info("Starting client handler cleanup");
        try {
            if (scanner != null) {
                scanner.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (moveSocket != null && !moveSocket.isClosed()) {
                moveSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error during cleanup", e);
            System.out.println("Error during cleanup: " + e.getMessage());
        }
        LOGGER.info("Client handler cleanup completed");
    }

    public void close() {
        try {
            // Mark as not alive first
            isAlive = false;

            if(isPlayer){
                if(isWhitePlayer){
                    System.out.println("game end was sent to white player");
                    LOGGER.info("Game end sent to white player");
                } else {
                    System.out.println("game end was sent to black player");
                    LOGGER.info("Game end sent to black player");
                }
            } else {
                System.out.println("game end was sent to spectator");
                LOGGER.info("Game end sent to spectator");
            }

            // Send GAME_END through heartbeat socket
            OutputStream outputStream1 = heartbeatSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream1, true);
            writer.println("GAME_END");
            writer.flush();
            writer.close();

            // Send game end through main socket
            printWriter.println("GAME_END");
            printWriter.println("Game has ended");
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
            LOGGER.log(Level.WARNING, "Error closing client handler", e);
            System.out.println("Error closing client handler: " + e.getMessage());
        }
    }
}