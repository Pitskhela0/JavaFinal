package serverSide;

import chess.model.BoardState;

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
    private static BoardState board;

    private static final AtomicBoolean gameFinished = new AtomicBoolean(false);
    private static final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private static ServerSocket serverSocket;

    public static BoardState getBoard() {
        return board;
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

        // start the game
        while (!gameFinished.get()){
            // initialize game

            if(!players.get(0).getIsAlive() || !players.get(1).getIsAlive()){
                System.out.println("White player is out");
                break;
            }

            Move whiteMove = players.get(0).handleMessagePlayer(COLOR_WHITE);

            recordMove(whiteMove);

            ClientHandler.broadcastPlayer(players.get(1), whiteMove); // send update to black player

            broadcast(whiteMove);


            if(!players.get(0).getIsAlive() || !players.get(1).getIsAlive()){
                System.out.println("black player is out");
                break;
            }

            // black player
            Move blackMove = players.get(1).handleMessagePlayer(COLOR_BLACK);

            recordMove(blackMove);

            broadcast(blackMove);

            ClientHandler.broadcastPlayer(players.get(0), blackMove); // send update to white player

        }
    }

    private static void recordMove(Move blackMove) {
        //
    }

    private static boolean isPlayerAlive(int playerIndex) {
        try {
            return playerIndex < players.size() && players.get(playerIndex).getIsAlive();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking player alive status", e);
            return false;
        }
    }

    private static Move handlePlayerMove(int playerIndex, String color) {
        try {
            if (playerIndex >= players.size()) {
                LOGGER.warning("Invalid player index: " + playerIndex);
                return new Move("error");
            }

            return players.get(playerIndex).handleMessagePlayer(color);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error handling player move for " + color, e);
            return new Move("error");
        }
    }

    private static boolean isGameEndingMove(Move move) {
        if (move == null) {
            return true;
        }

        String moveString = move.getMove();
        return "resign".equals(moveString) || "error".equals(moveString);
    }

    private static void broadcastMove(Move move, int targetPlayerIndex) {
        try {
            // Send to target player
            if (targetPlayerIndex < players.size()) {
                ClientHandler.broadcastPlayer(players.get(targetPlayerIndex), move);
            }

            // Send to all spectators
            broadcast(move);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error broadcasting move", e);
        }
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
        spectators = spectators.stream().filter((element) -> element.getIsAlive()).collect(Collectors.toList());
    }

    // Broadcasts moves to spectators
    public static void broadcast(Move move) {
        if (move == null) {
            LOGGER.warning("Cannot broadcast null move");
            return;
        }

        Thread.startVirtualThread(() -> {
            try {
                spectators = spectators.stream().filter((client -> client.getMoveSocket() != null)).collect(Collectors.toList());

                spectators.forEach((client) -> client.sendUpdate(move));

                LOGGER.fine("Broadcasted move to " + spectators.size() + " spectators");
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