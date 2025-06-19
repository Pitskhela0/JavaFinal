package serverSide;

import chess.model.BoardState;
import chess.model.Square;
import chess.model.pieces.Piece;
import shared.GameState;
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
    private static final int SERVER_PORT = 10000;
    private static final int SHUTDOWN_DELAY_MS = 500;
    private static final String COLOR_WHITE = "white";
    private static final String COLOR_BLACK = "black";
    private static final String HEARTBEAT_PORT_PREFIX = "HEARTBEAT_PORT:";

    private static List<ClientHandler> spectators = Collections.synchronizedList(new ArrayList<>());
    private static final List<ClientHandler> players = Collections.synchronizedList(new ArrayList<>());
    private static BoardState gameBoard; // Server maintains authoritative game state

    private static final AtomicBoolean gameFinished = new AtomicBoolean(false);
    private static final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private static ServerSocket serverSocket;

    public static BoardState getBoard() {
        return gameBoard;
    }

    public static void setGameStarted(boolean started) {
        gameStarted.set(started);
        LOGGER.info("Game started status changed to: " + started);
    }

    public static boolean isGameStarted() {
        return gameStarted.get();
    }

    public static List<ClientHandler> getSpectators() {
        return spectators;
    }

    public static List<ClientHandler> getPlayers() {
        return players;
    }

    public static void main(String[] args) {
        LOGGER.info("Starting server on port " + SERVER_PORT);

        try {
            initializeServer();
            runServer();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal server error", e);
        } finally {
            cleanup();
        }
    }

    private static void initializeServer() throws IOException {
        try {
            if (!gameFinished.get()) {
                serverSocket = new ServerSocket(SERVER_PORT);
                LOGGER.info("Server socket created successfully on port " + SERVER_PORT);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create server socket", e);
            throw new RuntimeException("Failed to initialize server", e);
        }
    }

    private static void runServer() {
        while (!gameFinished.get()) {
            try {
                LOGGER.info("Waiting for client connections...");
                Socket socket = serverSocket.accept();
                LOGGER.info("New client connected from: " + socket.getRemoteSocketAddress());

                handleNewClient(socket);
            } catch (IOException e) {
                if (!gameFinished.get()) {
                    LOGGER.log(Level.WARNING, "Error accepting client connection", e);
                }
                System.out.println("Server is closing");
                break;
            }
        }
    }

    private static void handleNewClient(Socket socket) {
        try {
            // Create heartbeat server socket
            ServerSocket heartbeatServerSocket = new ServerSocket(0); // 0 means OS will find available random port
            int heartbeatPort = heartbeatServerSocket.getLocalPort();

            LOGGER.info("Created heartbeat socket on port: " + heartbeatPort);

            // Send heartbeat port to client
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(HEARTBEAT_PORT_PREFIX + heartbeatPort);
            writer.flush();

            // Accept heartbeat connection
            Socket heartbeatSocket = heartbeatServerSocket.accept();
            LOGGER.info("Heartbeat connection established");
            System.out.println("Current players: " + players.size());

            // Create client handler
            ClientHandler clientHandler = new ClientHandler(socket, heartbeatSocket);

            // Start handling client in separate thread
            Thread.startVirtualThread(clientHandler::handleClient);

            // Close the heartbeat server socket as we only need one connection
            heartbeatServerSocket.close();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error handling new client", e);
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException closeEx) {
                LOGGER.log(Level.WARNING, "Error closing client socket", closeEx);
            }
        }
    }

    static void startGame() {
        LOGGER.info("Starting game with 2 players");
        gameStarted.set(true);

        // Initialize the game board on server
        gameBoard = new BoardState();

        // Send initial game state to both players and spectators
        GameState initialState = new GameState(gameBoard);
        broadcastGameState(initialState);

        LOGGER.info("Initial game state broadcasted");

        // Game loop
        int turnCount = 0;
        while (!gameFinished.get() && bothPlayersAlive()) {
            turnCount++;
            LOGGER.info("Starting turn " + turnCount);

            // White player's turn
            LOGGER.info("White player's turn");
            if (!handlePlayerTurn(0, true)) {
                LOGGER.info("White player turn failed, ending game");
                break;
            }

            // Check for game end after white's move
            if (isGameFinished()) {
                LOGGER.info("Game finished after white's move");
                break;
            }

            // Black player's turn
            LOGGER.info("Black player's turn");
            if (!handlePlayerTurn(1, false)) {
                LOGGER.info("Black player turn failed, ending game");
                break;
            }

            // Check for game end after black's move
            if (isGameFinished()) {
                LOGGER.info("Game finished after black's move");
                break;
            }
        }

        LOGGER.info("Game loop ended, calling endGame()");
        endGame();
    }

    private static boolean handlePlayerTurn(int playerIndex, boolean isWhite) {
        try {
            String color = isWhite ? COLOR_WHITE : COLOR_BLACK;
            LOGGER.info("Requesting move from " + color + " player");

            // Check if player is still alive before requesting move
            if (!isPlayerAlive(playerIndex)) {
                LOGGER.warning(color + " player is not alive");
                endGame();
                return false;
            }

            // Request move from current player
            ChessMove move = players.get(playerIndex).requestMoveFromPlayer();

            if (move == null) {
                LOGGER.warning("Received null move from " + color + " player");
                endGame();
                return false;
            }

            if (!move.isNormalMove()) {
                LOGGER.info("Game ending move received from " + color + ": " + move.getMoveType());
                endGame();
                return false;
            }

            // Validate and apply move on server's board
            if (validateAndApplyMove(move, isWhite)) {
                // Create updated game state
                GameState newState = new GameState(gameBoard);
                newState.setLastMove(move.toChessNotation());
                newState.incrementMoveCount();

                LOGGER.info("Valid move applied: " + move.toChessNotation());

                // Broadcast to all clients
                broadcastGameState(newState);

                // Check for game end conditions
                if (newState.isGameOver()) {
                    LOGGER.info("Game over detected: " + newState.getWinner());
                    // Don't call endGame() here, let the game loop handle it naturally
                    return false;
                }

                return true;
            } else {
                // Invalid move - ask player to try again
                LOGGER.warning("Invalid move attempted by " + color + ": " + move);
                players.get(playerIndex).sendInvalidMoveMessage();
                return handlePlayerTurn(playerIndex, isWhite); // Retry
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in player turn for " + (isWhite ? "white" : "black"), e);
            return false;
        }
    }

    private static boolean validateAndApplyMove(ChessMove move, boolean isWhite) {
        try {
            Square[][] board = gameBoard.getSquareArray();
            Square fromSquare = board[move.getFromRow()][move.getFromCol()];
            Square toSquare = board[move.getToRow()][move.getToCol()];

            Piece piece = fromSquare.getOccupyingPiece();

            // Validate move
            if (piece == null) {
                LOGGER.warning("No piece at source position");
                return false;
            }

            if ((piece.getColor() == 1) != isWhite) {
                LOGGER.warning("Wrong color piece moved");
                return false;
            }

            if (!piece.getLegalMoves(gameBoard).contains(toSquare)) {
                LOGGER.warning("Illegal move for piece");
                return false;
            }

            if (!gameBoard.isKingSafeAfterMove(piece, toSquare)) {
                LOGGER.warning("Move would put king in check");
                return false;
            }

            // Apply move
            gameBoard.commitMove(fromSquare, toSquare, piece);
            LOGGER.info("Move applied successfully");

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating move", e);
            return false;
        }
    }

    private static boolean bothPlayersAlive() {
        return players.size() >= 2 &&
                players.get(0).getIsAlive() &&
                players.get(1).getIsAlive();
    }

    private static boolean isPlayerAlive(int playerIndex) {
        try {
            return playerIndex >= 0 &&
                    playerIndex < players.size() &&
                    players.get(playerIndex) != null &&
                    players.get(playerIndex).getIsAlive();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking player alive status for index " + playerIndex, e);
            return false;
        }
    }

    private static boolean isGameFinished() {
        return gameFinished.get() || gameBoard == null || !bothPlayersAlive();
    }

    private static void broadcastGameState(GameState gameState) {
        LOGGER.info("Broadcasting game state to all clients");

        // Send to all players and spectators
        List<ClientHandler> allClients = new ArrayList<>(players);
        allClients.addAll(spectators);

        allClients.forEach(client -> {
            try {
                client.sendGameState(gameState);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending game state to client", e);
            }
        });
    }

    public static void endGame() {
        if (gameFinished.getAndSet(true)) {
            return; // Already ending/ended
        }

        LOGGER.info("Ending game");
        System.out.println("Closing the game :: endGame()");

        try {
            // Send close message to all clients
            List<ClientHandler> allClients = new ArrayList<>();
            allClients.addAll(players);
            allClients.addAll(spectators);

            allClients.forEach(client -> {
                try {
                    client.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error closing client", e);
                }
            });

            // Use a separate thread to close the server socket after a brief delay
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(SHUTDOWN_DELAY_MS);
                    closeServerSocket();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error during server shutdown", e);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during game end process", e);
        }
    }

    private static void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Server socket closed");
                System.out.println("Server socket closed");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }
    }

    public static void kickoutSpectator(){
        spectators = spectators.stream()
                .filter(ClientHandler::getIsAlive)
                .collect(Collectors.toList());
    }

    // Broadcasts game state to spectators
    public static void broadcast(GameState gameState) {
        if (gameState == null) {
            LOGGER.warning("Cannot broadcast null game state");
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
                        LOGGER.log(Level.WARNING, "Error broadcasting to spectator", e);
                    }
                });

                LOGGER.fine("Broadcasted game state to " + spectators.size() + " spectators");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during broadcast", e);
            }
        });
    }

    private static void cleanup() {
        LOGGER.info("Server cleanup started");

        try {
            // Clear client lists
            players.clear();
            spectators.clear();

            // Close server socket if not already closed
            closeServerSocket();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during server cleanup", e);
        }

        LOGGER.info("Server cleanup completed");
    }
}