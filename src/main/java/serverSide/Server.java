package serverSide;

import chess.model.BoardState;
import chess.model.Square;
import chess.model.pieces.Piece;import shared.GameState;
import shared.ChessMove;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private int SERVER_PORT;
    private int GAME_ID;
    private final int SHUTDOWN_DELAY_MS = 500;
    private final String COLOR_WHITE = "white";
    private final String COLOR_BLACK = "black";
    private final String HEARTBEAT_PORT_PREFIX = "HEARTBEAT_PORT:";

    private List<ClientHandler> spectators = Collections.synchronizedList(new ArrayList<>());
    private final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    private BoardState gameBoard; // Server maintains authoritative game state

    private final AtomicBoolean gameFinished = new AtomicBoolean(false);
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private ServerSocket serverSocket;

    public Server(int gameID){
        GAME_ID = gameID;
    }

    public BoardState getBoard() {
        return gameBoard;
    }

    public void setGameStarted(boolean started) {
        gameStarted.set(started);
        LOGGER.info("Server - Game started status changed to: " + started);
    }

    public boolean isGameStarted() {
        return gameStarted.get();
    }

    public List<ClientHandler> getSpectators() {
        return spectators;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public void start(){

        // RESET ALL STATIC FLAGS FOR NEW GAME
        gameFinished.set(false);
        gameStarted.set(false);

        // Clear any leftover client lists
        synchronized (players) {
            players.clear();
        }
        synchronized (spectators) {
            spectators.clear();
        }

        // Reset game board
        gameBoard = null;

        try {
            initializeServer();
            LOGGER.info("Server - Starting server on port " + SERVER_PORT);

            runServer();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server - Fatal server error", e);
        } finally {
            cleanup();
        }
    }

//    public static void main(String[] args) {
//        LOGGER.info("Server - Starting server on port " + SERVER_PORT);
//
//        try {
//            initializeServer();
//            runServer();
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Server - Fatal server error", e);
//        } finally {
//            cleanup();
//        }
//    }

    private void initializeServer() throws IOException {
        try {
            if (!gameFinished.get()) {
                // there is no need to wait for available port for out app right now, since many users will not connect
                serverSocket = new ServerSocket(0); // finds free available port in OS
//                serverSocket = new ServerSocket(10000);
                SERVER_PORT = serverSocket.getLocalPort();
                LOGGER.info("Server - Server socket created successfully on port " + SERVER_PORT);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server - Failed to create server socket", e);
            throw new RuntimeException("Failed to initialize server", e);
        }
    }

    public int getPort(){
        return SERVER_PORT;
    }

    private void runServer() {
        while (!gameFinished.get()) {
            try {
                LOGGER.info("Server - Waiting for client connections...");
                Socket socket = serverSocket.accept();
                LOGGER.info("Server - New client connected from: " + socket.getRemoteSocketAddress());

                handleNewClient(socket);
            } catch (IOException e) {
                if (!gameFinished.get()) {
                    LOGGER.log(Level.WARNING, "Server - Error accepting client connection", e);
                }
                System.out.println("Server is closing");
                break;
            }
        }
    }

    private void handleNewClient(Socket socket) {
        try {
            // Create heartbeat server socket
            ServerSocket heartbeatServerSocket = new ServerSocket(0); // 0 means OS will find available random port
            int heartbeatPort = heartbeatServerSocket.getLocalPort();

            LOGGER.info("Server - Created heartbeat socket on port: " + heartbeatPort);

            // Send heartbeat port to client
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(HEARTBEAT_PORT_PREFIX + heartbeatPort);
            writer.flush();

            // Accept heartbeat connection
            Socket heartbeatSocket = heartbeatServerSocket.accept();
            LOGGER.info("Server - Heartbeat connection established");
            System.out.println("Current players: " + players.size());

            // Create client handler
            ClientHandler clientHandler = new ClientHandler(socket, heartbeatSocket, this);

            // Start handling client in separate thread
            Thread.startVirtualThread(clientHandler::handleClient);

            // Close the heartbeat server socket as we only need one connection
            heartbeatServerSocket.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server - Error handling new client", e);
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException closeEx) {
                LOGGER.log(Level.WARNING, "Server - Error closing client socket", closeEx);
            }
        }
    }

    public void startGame() {
        LOGGER.info("Server - Starting game with 2 players");
        gameStarted.set(true);

        // Initialize the game board on server
        gameBoard = new BoardState();

        // Send initial game state to both players and spectators
        GameState initialState = new GameState(gameBoard);
        broadcastGameState(initialState);

        LOGGER.info("Server - Initial game state broadcasted");

        // Game loop
        int turnCount = 0;
        while (!gameFinished.get() && bothPlayersAlive()) {
            turnCount++;
            LOGGER.info("Server - Starting turn " + turnCount);

            // White player's turn
            LOGGER.info("Server - White player's turn");
            if (!handlePlayerTurn(0, true)) {
                LOGGER.info("Server - White player turn failed, ending game");
                break;
            }

            // Check for game end after white's move
            if (isGameFinished()) {
                LOGGER.info("Server - Game finished after white's move");
                break;
            }

            // Black player's turn
            LOGGER.info("Server - Black player's turn");
            if (!handlePlayerTurn(1, false)) {
                LOGGER.info("Server - Black player turn failed, ending game");
                break;
            }

            // Check for game end after black's move
            if (isGameFinished()) {
                LOGGER.info("Server - Game finished after black's move");
                break;
            }
        }

        LOGGER.info("Server - Game loop ended, calling endGame()");
        endGame();
    }

    private boolean handlePlayerTurn(int playerIndex, boolean isWhite) {
        try {
            String color = isWhite ? COLOR_WHITE : COLOR_BLACK;
            LOGGER.info("Server - Requesting move from " + color + " player");

            // Check if player is still alive before requesting move
            if (!isPlayerAlive(playerIndex)) {
                LOGGER.warning("Server - " + color + " player is not alive");
                endGame();
                return false;
            }

            // Request move from current player
            ChessMove move = players.get(playerIndex).requestMoveFromPlayer();

            if (move == null) {
                LOGGER.warning("Server - Received null move from " + color + " player");
                endGame();
                return false;
            }

            if (!move.isNormalMove()) {
                LOGGER.info("Server - Game ending move received from " + color + ": " + move.getMoveType());
                endGame();
                return false;
            }

            // Validate and apply move on server's board
            if (validateAndApplyMove(move, isWhite)) {
                // Create updated game state
                GameState newState = new GameState(gameBoard);
                newState.setLastMove(move.toChessNotation());
                newState.incrementMoveCount();

                LOGGER.info("Server - Valid move applied: " + move.toChessNotation());

                // Broadcast to all clients
                broadcastGameState(newState);

                // Check for game end conditions
                if (newState.isGameOver()) {
                    LOGGER.info("Server - Game over detected: " + newState.getWinner());
                    // Don't call endGame() here, let the game loop handle it naturally
                    return false;
                }

                return true;
            } else {
                // Invalid move - ask player to try again
                LOGGER.warning("Server - Invalid move attempted by " + color + ": " + move);
                players.get(playerIndex).sendInvalidMoveMessage();
                return handlePlayerTurn(playerIndex, isWhite); // Retry
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Server - Error in player turn for " + (isWhite ? "white" : "black"), e);
            return false;
        }
    }

    private boolean validateAndApplyMove(ChessMove move, boolean isWhite) {
        try {
            Square[][] board = gameBoard.getSquareArray();
            Square fromSquare = board[move.getFromRow()][move.getFromCol()];
            Square toSquare = board[move.getToRow()][move.getToCol()];

            Piece piece = fromSquare.getOccupyingPiece();

            // Validate move
            if (piece == null) {
                LOGGER.warning("Server - No piece at source position");
                return false;
            }

            if ((piece.getColor() == 1) != isWhite) {
                LOGGER.warning("Server - Wrong color piece moved");
                return false;
            }

            if (!piece.getLegalMoves(gameBoard).contains(toSquare)) {
                LOGGER.warning("Server - Illegal move for piece");
                return false;
            }

            if (!gameBoard.isKingSafeAfterMove(piece, toSquare)) {
                LOGGER.warning("Server - Move would put king in check");
                return false;
            }

            // Apply move
            gameBoard.commitMove(fromSquare, toSquare, piece);
            LOGGER.info("Server - Move applied successfully");

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Server - Error validating move", e);
            return false;
        }
    }

    private boolean bothPlayersAlive() {
        return players.size() >= 2 &&
                players.get(0).getIsAlive() &&
                players.get(1).getIsAlive();
    }

    private boolean isPlayerAlive(int playerIndex) {
        try {
            return playerIndex >= 0 &&
                    playerIndex < players.size() &&
                    players.get(playerIndex) != null &&
                    players.get(playerIndex).getIsAlive();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Server - Error checking player alive status for index " + playerIndex, e);
            return false;
        }
    }

    private boolean isGameFinished() {
        return gameFinished.get() || gameBoard == null || !bothPlayersAlive();
    }

    private void broadcastGameState(GameState gameState) {
        LOGGER.info("Server - Broadcasting game state to all clients");

        // Send to all players and spectators
        List<ClientHandler> allClients = new ArrayList<>(players);
        allClients.addAll(spectators);

        allClients.forEach(client -> {
            try {
                client.sendGameState(gameState);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Server - Error sending game state to client", e);
            }
        });
    }

//    public static void endGame() {
//        if (gameFinished.getAndSet(true)) {
//            return; // Already ending/ended
//        }
//
//        LOGGER.info("Server - Ending game");
//        System.out.println("Closing the game :: endGame()");
//
//        try {
//            // Send close message to all clients
//            List<ClientHandler> allClients = new ArrayList<>();
//            allClients.addAll(players);
//            allClients.addAll(spectators);
//
//            allClients.forEach(client -> {
//                try {
//                    client.close();
//                } catch (Exception e) {
//                    LOGGER.log(Level.WARNING, "Server - Error closing client", e);
//                }
//            });
//
//            // Close server socket immediately (don't wait)
//            closeServerSocket();
//
//            // Clear client lists
//            players.clear();
//            spectators.clear();
//
//            // Reset game state
//            gameBoard = null;
//            gameStarted.set(false);
//
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Server - Error during game end process", e);
//        }
//
//        System.out.println("Server shutdown completed");
//    }
public void endGame() {
    if (gameFinished.getAndSet(true)) {
        return; // Already ending/ended
    }

    LOGGER.info("Server - Ending game");
    System.out.println("Closing the game :: endGame()");

    try {
        // Send close message to all clients
        List<ClientHandler> allClients = new ArrayList<>();
        synchronized (players) {
            allClients.addAll(players);
        }
        synchronized (spectators) {
            allClients.addAll(spectators);
        }

        allClients.forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Server - Error closing client", e);
            }
        });

        // Close server socket immediately
        closeServerSocket();

        // Clear client lists
        synchronized (players) {
            players.clear();
        }
        synchronized (spectators) {
            spectators.clear();
        }

        // Reset game state
        gameBoard = null;
        gameStarted.set(false);

    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Server - Error during game end process", e);
    }

    System.out.println("Server shutdown completed");
}

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Server - Server socket closed");
                System.out.println("Server socket closed");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Server - Error closing server socket", e);
        }
    }

    public void kickoutSpectator(){
        spectators = spectators.stream()
                .filter(ClientHandler::getIsAlive)
                .collect(Collectors.toList());
    }

    // Broadcasts game state to spectators
    public void broadcast(GameState gameState) {
        if (gameState == null) {
            LOGGER.warning("Server - Cannot broadcast null game state");
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                spectators = spectators.stream()
                        .filter(client -> client.getMoveSocket() != null)
                        .collect(Collectors.toList());

                spectators.forEach(client -> {
                    try {
                        client.sendGameState(gameState);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Server - Error broadcasting to spectator", e);
                    }
                });

                LOGGER.fine("Server - Broadcasted game state to " + spectators.size() + " spectators");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Server - Error during broadcast", e);
            }
        });
    }

    private void cleanup() {
        LOGGER.info("Server - Server cleanup started");

        try {
            // Clear client lists
            players.clear();
            spectators.clear();

            // Close server socket if not already closed
            closeServerSocket();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Server - Error during server cleanup", e);
        }

        LOGGER.info("Server - Server cleanup completed");
    }
}