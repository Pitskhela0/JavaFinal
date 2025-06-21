package clientSide.clients;

import clientSide.utils.ServerConnector;
import shared.GameState;
import shared.ChessMove;
import chess.view.NetworkGameWindow;
import startMenu.ClientConnection;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerClient {
    private boolean isWhite;
    private ServerConnector serverConnector;
    private Scanner serverScanner;
    private PrintWriter printWriter;
    private Scanner userInputScanner;
    private NetworkGameWindow gameWindow;
    private CountDownLatch windowCreatedLatch = new CountDownLatch(1);
    private AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private ClientConnection clientConnection;

    public PlayerClient(boolean isWhite, ServerConnector serverConnector, Scanner serverScanner,
                        PrintWriter printWriter, Scanner userInputScanner, ClientConnection clientConnection){
        this.isWhite = isWhite;
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;
        this.printWriter = printWriter;
        this.userInputScanner = userInputScanner;
        this.clientConnection = clientConnection;

        initializeGameWindow();
    }

    private void initializeGameWindow() {
        SwingUtilities.invokeLater(() -> {
            try {
                String colorString = isWhite ? "white" : "black";
                gameWindow = new NetworkGameWindow("player", colorString);
                gameWindow.setPlayerClient(this);

                // Set proper close operation - DON'T exit immediately
                gameWindow.getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                // Add window listener for cleanup
                gameWindow.getFrame().addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        System.out.println("Game window closing, cleaning up...");
                        requestShutdown();
                    }
                });

                System.out.println("Game window created for " + colorString + " player");
                windowCreatedLatch.countDown(); // Signal that window is ready

            } catch (Exception e) {
                System.err.println("Error creating game window: " + e.getMessage());
                e.printStackTrace();
                windowCreatedLatch.countDown(); // Signal completion even on error
            }
        });

        // Wait for window creation to complete
        try {
            windowCreatedLatch.await(); // Wait up to 5 seconds
            Thread.sleep(100); // Small delay to ensure window is fully rendered
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for window creation");
        }
    }

    public void requestShutdown() {
        if (shutdownRequested.getAndSet(true)) {
            return; // Already shutting down
        }

        System.out.println("Shutdown requested, cleaning up all resources...");

        // Create shutdown thread to handle cleanup
        Thread shutdownThread = new Thread(() -> {
            try {
                // 1. Disconnect from server gracefully
                if (serverConnector != null) {
                    serverConnector.getIsConnected().set(false);
                }

                // 2. Close network resources
                closeAllResources();

                // 3. Close window on EDT
                SwingUtilities.invokeLater(() -> {
                    if (gameWindow != null) {
                        gameWindow.getFrame().dispose();
                    }
                });

                // 4. Force exit after brief delay
                Thread.sleep(500);
                System.out.println("Exiting application...");
//                System.exit(0);

                // reshow the start menu
                clientConnection.showStartMenu();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
                System.exit(1); // Force exit even on error
            }
        });

        shutdownThread.setName("Shutdown-Thread");
        shutdownThread.setDaemon(false); // Ensure it completes before JVM exit
        shutdownThread.start();
    }

    public void runPlayer() {
        System.out.println("You are now a " + (isWhite ? "white" : "black") + " player. Waiting for game to start...");

        while (serverConnector.getIsConnected().get() && !shutdownRequested.get()) {
            try {
                // Check if scanner has data available (non-blocking check)
                if (!serverScanner.hasNextLine()) {
                    Thread.sleep(100); // Brief pause to prevent busy waiting
                    continue;
                }

                String messageFromServer = serverScanner.nextLine();

                // Check for shutdown during processing
                if (shutdownRequested.get()) {
                    break;
                }

                System.out.println("Received from server: " + messageFromServer);

                if (messageFromServer.equals("GAME_STATE_UPDATE")) {
                    // This is a game state update
                    String gameStateData = serverScanner.nextLine();
                    GameState gameState = deserializeGameState(gameStateData);

                    if (gameWindow != null && !shutdownRequested.get()) {
                        SwingUtilities.invokeLater(() -> {
                            gameWindow.updateGameState(gameState);
                        });
                    }
                    System.out.println("Game state updated: " + gameState);

                } else if (messageFromServer.equals("REQUEST_MOVE")) {
                    // Server is asking for our move
                    System.out.println("Your turn! Make your move on the board or type 'resign'");

                } else if (messageFromServer.equals("INVALID_MOVE")) {
                    // Server rejected our move
                    String errorMessage = serverScanner.nextLine();
                    System.out.println("Invalid move: " + errorMessage);

                    if (gameWindow != null && !shutdownRequested.get()) {
                        SwingUtilities.invokeLater(() -> {
                            gameWindow.showError("Invalid move: " + errorMessage);
                        });
                    }

                } else if (messageFromServer.equals("GAME_END")) {
                    // Game has ended
                    String endMessage = serverScanner.nextLine();
                    System.out.println("Game ended: " + endMessage);

                    if (gameWindow != null && !shutdownRequested.get()) {
                        SwingUtilities.invokeLater(() -> {
                            gameWindow.showInfo("Game Over: " + endMessage);
                        });
                    }
                    break;

                } else {
                    // Other server messages
                    System.out.println("Server: " + messageFromServer);
                }
            } catch (Exception e) {
                if (!shutdownRequested.get()) {
                    System.out.println("Connection lost or error occurred: " + e.getMessage());
                }
                break;
            }
        }

        System.out.println("Player client loop ended");

        // If we exit the loop and shutdown wasn't requested, request it now
        if (!shutdownRequested.get()) {
            requestShutdown();
        }
    }

    // Called by the board panel when player makes a move
    public void sendMoveToServer(ChessMove move) {
        if (shutdownRequested.get()) return;

        try {
            printWriter.println(move.toChessNotation());
            printWriter.flush();
            System.out.println("Move sent to server: " + move.toChessNotation());

        } catch (Exception e) {
            System.out.println("Error sending move to server: " + e.getMessage());
        }
    }

    // Called by the board panel to send resignation
    public void sendResignation() {
        if (shutdownRequested.get()) return;

        try {
            printWriter.println("resign");
            printWriter.flush();
            System.out.println("Resignation sent to server");
        } catch (Exception e) {
            System.out.println("Error sending resignation to server: " + e.getMessage());
        }
    }

    // Deserialize game state from server format
    private GameState deserializeGameState(String data) {
        GameState gameState = new GameState();

        try {
            if (data == null || data.trim().isEmpty()) {
                return gameState;
            }

            String[] parts = data.split("\\|", -1);

            // Parse board state
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String[] pieces = parts[0].split(";");
                String[][] board = gameState.getBoard();

                for (String piece : pieces) {
                    if (!piece.trim().isEmpty()) {
                        String[] pieceData = piece.split(",");
                        if (pieceData.length >= 3) {
                            int row = Integer.parseInt(pieceData[0]);
                            int col = Integer.parseInt(pieceData[1]);
                            String pieceType = pieceData[2];
                            board[row][col] = pieceType;
                        }
                    }
                }
            }

            // Parse metadata
            if (parts.length > 1 && !parts[1].isEmpty()) {
                String[] metadata = parts[1].split(";");
                for (String meta : metadata) {
                    if (meta.contains(":")) {
                        String[] keyValue = meta.split(":", 2);
                        if (keyValue.length >= 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();

                            switch (key) {
                                case "whiteTurn":
                                    gameState.setWhiteTurn(Boolean.parseBoolean(value));
                                    break;
                                case "whiteInCheck":
                                    gameState.setWhiteInCheck(Boolean.parseBoolean(value));
                                    break;
                                case "blackInCheck":
                                    gameState.setBlackInCheck(Boolean.parseBoolean(value));
                                    break;
                                case "gameOver":
                                    gameState.setGameOver(Boolean.parseBoolean(value));
                                    break;
                                case "winner":
                                    gameState.setWinner(value.equals("none") ? null : value);
                                    break;
                                case "moveCount":
                                    gameState.setMoveCount(Integer.parseInt(value));
                                    break;
                                case "lastMove":
                                    gameState.setLastMove(value.equals("none") ? null : value);
                                    break;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (!shutdownRequested.get()) {
                System.out.println("Error deserializing game state: " + e.getMessage());
            }
        }

        return gameState;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isMyTurn(GameState gameState) {
        return gameState.isWhiteTurn() == isWhite;
    }

    private void closeAllResources(){
        System.out.println("Closing all client resources...");

        try {
            // Close server connector first (this stops heartbeat threads)
            if (serverConnector != null) {
                serverConnector.shutdown();
            }

            // Close network connections
            if (printWriter != null) {
                printWriter.close();
            }

            if (serverScanner != null) {
                serverScanner.close();
            }

            if (userInputScanner != null) {
                userInputScanner.close();
            }

            System.out.println("All client resources closed");

        } catch (Exception e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }
}