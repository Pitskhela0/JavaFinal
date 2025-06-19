package clientSide.clients;

import clientSide.utils.ServerConnector;
import shared.GameState;
import shared.ChessMove;
import chess.view.NetworkGameWindow;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.Scanner;

public class PlayerClient {
    private boolean isWhite;
    private ServerConnector serverConnector;
    private Scanner serverScanner;
    private PrintWriter printWriter;
    private Scanner userInputScanner;
    private NetworkGameWindow gameWindow;

    public PlayerClient(boolean isWhite, ServerConnector serverConnector, Scanner serverScanner,
                        PrintWriter printWriter, Scanner userInputScanner){
        this.isWhite = isWhite;
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;
        this.printWriter = printWriter;
        this.userInputScanner = userInputScanner;

        initializeGameWindow();
    }

    private void initializeGameWindow() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                String colorString = isWhite ? "white" : "black";
                gameWindow = new NetworkGameWindow("player", colorString);
                gameWindow.setPlayerClient(this); // Set the player client reference
                System.out.println("Game window created for " + colorString + " player");
            });
        } catch (Exception e) {
            System.out.println("Error creating game window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void runPlayer() {
        System.out.println("You are now a " + (isWhite ? "white" : "black") + " player. Waiting for game to start...");

        while (serverConnector.getIsConnected().get()) {
            try {
                String messageFromServer = serverScanner.nextLine();
                System.out.println("Received from server: " + messageFromServer);

                if (messageFromServer.equals("GAME_STATE_UPDATE")) {
                    // This is a game state update
                    String gameStateData = serverScanner.nextLine();
                    GameState gameState = deserializeGameState(gameStateData);

                    if (gameWindow != null) {
                        gameWindow.updateGameState(gameState);
                    } else {
                        System.out.println("Warning: gameWindow is null, cannot update game state");
                    }
                    System.out.println("Game state updated: " + gameState);

                } else if (messageFromServer.equals("REQUEST_MOVE")) {
                    // Server is asking for our move
                    System.out.println("Your turn! Make your move on the board or type 'resign'");

                    // The move will be sent through the GUI when player drags a piece
                    // or we can handle console input as backup

                } else if (messageFromServer.equals("INVALID_MOVE")) {
                    // Server rejected our move
                    String errorMessage = serverScanner.nextLine();
                    System.out.println("Invalid move: " + errorMessage);

                    gameWindow.showError("Invalid move: " + errorMessage);

                } else if (messageFromServer.equals("GAME_END")) {
                    // Game has ended
                    String endMessage = serverScanner.nextLine();
                    System.out.println("Game ended: " + endMessage);

                    gameWindow.showInfo("Game Over: " + endMessage);
                    break;

                } else {
                    // Other server messages
                    System.out.println("Server: " + messageFromServer);
                }
            } catch (Exception e) {
                System.out.println("Connection lost or error occurred: " + e.getMessage());
                serverConnector.getIsConnected().set(false);
                closeAllResources();
                break;
            }
        }
    }

    // Called by the board panel when player makes a move
    public void sendMoveToServer(ChessMove move) {
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
            System.out.println("Deserializing game state data: " + data);

            if (data == null || data.trim().isEmpty()) {
                System.out.println("Empty game state data received");
                return gameState;
            }

            String[] parts = data.split("\\|", -1); // -1 to keep empty strings
            System.out.println("Split into " + parts.length + " parts");

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
                        String[] keyValue = meta.split(":", 2); // Limit to 2 parts
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
            System.out.println("Error deserializing game state: " + e.getMessage());
            e.printStackTrace();
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
        try {
            if (gameWindow != null) {
                gameWindow.closeWindow();
            }

            if (serverScanner != null) {
                serverScanner.close();
            }

            if (printWriter != null) {
                printWriter.close();
            }

            if (userInputScanner != null) {
                userInputScanner.close();
            }

        } catch (Exception e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }
}