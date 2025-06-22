package clientSide.clients;

import chess.model.BoardState;
import chess.model.Square;
import chess.model.pieces.*;
import clientSide.utils.ServerConnector;
import shared.GameState;
import shared.ChessMove;
import chess.view.NetworkGameWindow;
import startMenu.ClientConnection;
import startMenu.buttonFunctions.PlayWithBotButton;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BotPlayerClient {
    private boolean isWhite;
    private ServerConnector serverConnector;
    private Scanner serverScanner;
    private PrintWriter printWriter;
    private Scanner userInputScanner;
    private NetworkGameWindow gameWindow;
    private CountDownLatch windowCreatedLatch = new CountDownLatch(1);
    private AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private ClientConnection clientConnection;
    private GameState gameState;
    private Socket clientSocket;

    public BotPlayerClient(boolean isWhite, ServerConnector serverConnector, Scanner serverScanner,
                        PrintWriter printWriter, Scanner userInputScanner, ClientConnection clientConnection, Socket clientSocket){
        this.isWhite = isWhite;
        this.serverConnector = serverConnector;
        this.serverScanner = serverScanner;
        this.printWriter = printWriter;
        this.userInputScanner = userInputScanner;
        this.clientConnection = clientConnection;
        this.clientSocket = clientSocket;
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

    public void runBot() {
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
                    gameState = deserializeGameState(gameStateData);

                    System.out.println("Game state updated in bot player");

                } else if (messageFromServer.equals("REQUEST_MOVE")) {
                    // Server is asking for our move
                    System.out.println("Your turn! Make your move on the board or type 'resign'");


                    ChessMove botMove = findLegalMove();

                    sendMoveToServer(botMove);

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

    private ChessMove findLegalMove() {
        String[][] stringBoard = gameState.getBoard();
        Square[][] squareBoard = generateSquareBoard(stringBoard);

        BoardState currentBoard = new BoardState(squareBoard);

        List<Piece> blackPieces = currentBoard.getBlackPieces();

        for(Piece piece : blackPieces){
            List<Square> legalMoves = piece.getLegalMoves(currentBoard);

            for (Square targetSquare : legalMoves){
                if(currentBoard.isKingSafeAfterMove(piece, targetSquare)){
                    return new ChessMove(
                            piece.getPosition().getYNum(),
                            piece.getPosition().getXNum(),
                            targetSquare.getYNum(),
                            targetSquare.getXNum());
                }
            }
        }
        return null;
    }

    private Square[][] generateSquareBoard(String[][] stringBoard) {
        Square[][] board = new Square[8][8];

        // First pass: Create all squares with proper colors
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Square color follows same pattern as original BoardState
                int squareColor = ((col + row) % 2 == 0) ? 1 : 0;
                board[row][col] = new Square(col, row, squareColor);
            }
        }

        // Second pass: Create and place pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String pieceString = stringBoard[row][col];

                if (pieceString != null && !pieceString.isEmpty()) {
                    Piece piece = createPieceFromString(pieceString, board[row][col]);
                    if (piece != null) {
                        board[row][col].setOccupyingPiece(piece);
                    }
                }
            }
        }

        return board;
    }

    private Piece createPieceFromString(String pieceString, Square square) {
        if (pieceString == null || pieceString.length() < 2) {
            return null;
        }

        // Extract color and piece type
        int pieceColor = pieceString.charAt(0) == 'w' ? 1 : 0; // 1 = white, 0 = black
        String pieceType = pieceString.substring(1); // Everything after color char

        // Create image path
        String imagePath = "/images/" + pieceString + ".png";

        // Create appropriate piece based on type
        switch (pieceType) {
            case "pawn":
                return new Pawn(pieceColor, square, imagePath);
            case "rook":
                return new Rook(pieceColor, square, imagePath);
            case "knight":
                return new Knight(pieceColor, square, imagePath);
            case "bishop":
                return new Bishop(pieceColor, square, imagePath);
            case "queen":
                return new Queen(pieceColor, square, imagePath);
            case "king":
                return new King(pieceColor, square, imagePath);
            default:
                System.err.println("Unknown piece type: " + pieceType);
                return null;
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