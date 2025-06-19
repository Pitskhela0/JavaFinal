package clientSide.clients;

import clientSide.utils.ServerConnector;
import shared.GameState;
import chess.view.NetworkGameWindow;

import javax.swing.*;
import java.util.Scanner;

public class SpectatorClient {
    private ServerConnector serverConnector;
    private Scanner serverScanner;
    private NetworkGameWindow gameWindow;

    public SpectatorClient(ServerConnector serverConnector, Scanner serverScanner){
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;

        initializeSpectatorWindow();
    }

    private void initializeSpectatorWindow() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                gameWindow = new NetworkGameWindow("spectator", "spectator");
                System.out.println("Spectator window created");
            });
        } catch (Exception e) {
            System.out.println("Error creating spectator window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clientSpectator() {
        System.out.println("You are now a spectator. Watching the game...");

        try {
            while (serverConnector.getIsConnected().get()) {
                String messageFromServer = serverScanner.nextLine();
                System.out.println("Spectator received: " + messageFromServer);

                if (messageFromServer.equals("GAME_STATE_UPDATE")) {
                    // This is a game state update
                    String gameStateData = serverScanner.nextLine();
                    GameState gameState = deserializeGameState(gameStateData);

                    if (gameWindow != null) {
                        gameWindow.updateGameState(gameState);
                    } else {
                        System.out.println("Warning: gameWindow is null for spectator");
                    }
                    System.out.println("Spectator game state updated: " + gameState);

                } else if (messageFromServer.equals("GAME_END")) {
                    // Game has ended
                    String endMessage = serverScanner.nextLine();
                    System.out.println("Game ended: " + endMessage);

                    gameWindow.showInfo("Game Over: " + endMessage);
                    break;

                } else {
                    // Handle legacy message format for backward compatibility
                    if (messageFromServer.equals("resign")) {
                        System.out.println("Game ended - resignation");
                        gameWindow.showInfo("Game ended by resignation");
                        break;
                    } else {
                        System.out.println("Game update: " + messageFromServer);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Connection lost or error occurred: " + e.getMessage());
            serverConnector.getIsConnected().set(false);
            closeAllResources();
        }
    }

    // Deserialize game state from server format
    private GameState deserializeGameState(String data) {
        GameState gameState = new GameState();

        try {
            System.out.println("Spectator deserializing game state data: " + data);

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

    private void closeAllResources(){
        try {
            if (gameWindow != null) {
                gameWindow.closeWindow();
            }

            if (serverScanner != null) {
                serverScanner.close();
            }

        } catch (Exception e) {
            System.out.println("Error closing spectator resources: " + e.getMessage());
        }
    }
}