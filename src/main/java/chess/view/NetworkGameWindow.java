package chess.view;

import clientSide.clients.PlayerClient;
import shared.GameState;

import javax.swing.*;
import java.awt.*;

public class NetworkGameWindow {
    private final JFrame gameWindow;
    private final NetworkBoardPanel boardPanel;
    private final String playerRole;
    private final String playerColor; // For players: "white", "black", for spectators: "spectator"
    private PlayerClient playerClient; // Will be set later for players

    // Status labels
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JLabel moveCountLabel;

    public NetworkGameWindow(String playerRole, String playerColor) {
        this.playerRole = playerRole;
        this.playerColor = playerColor;

        gameWindow = new JFrame("Chess - " + getWindowTitle());
        gameWindow.setLayout(new BorderLayout());
        gameWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Don't exit immediately

        // Add window listener for X button
        gameWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleWindowClose();
            }
        });

        // Create board panel (without PlayerClient for now)
        boardPanel = new NetworkBoardPanel();

        // Create info panel
        JPanel infoPanel = createInfoPanel();

        // Create button panel
//        JPanel buttonPanel = createButtonPanel();

        gameWindow.add(infoPanel, BorderLayout.NORTH);
        gameWindow.add(boardPanel, BorderLayout.CENTER);
//        gameWindow.add(buttonPanel, BorderLayout.SOUTH);

        gameWindow.pack();
        gameWindow.setResizable(false);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setVisible(true);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Role and color info
        JLabel roleLabel = new JLabel("Role: " + playerRole +
                (playerRole.equals("player") ? " (" + playerColor + ")" : ""),
                SwingConstants.CENTER);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Game status
        statusLabel = new JLabel("Waiting for game to start...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Turn and move info
        JPanel gameInfoPanel = new JPanel(new GridLayout(1, 2));
        turnLabel = new JLabel("Turn: -", SwingConstants.CENTER);
        moveCountLabel = new JLabel("Move: 0", SwingConstants.CENTER);
        gameInfoPanel.add(turnLabel);
        gameInfoPanel.add(moveCountLabel);

        infoPanel.add(roleLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(gameInfoPanel);

        return infoPanel;
    }

//    private JPanel createButtonPanel() {
//        JPanel buttonPanel = new JPanel(new FlowLayout());
//
//        if (playerRole.equals("player")) {
//            JButton resignButton = new JButton("Resign");
//            resignButton.addActionListener(e -> {
//                int confirm = JOptionPane.showConfirmDialog(
//                        gameWindow,
//                        "Are you sure you want to resign?",
//                        "Confirm Resignation",
//                        JOptionPane.YES_NO_OPTION
//                );
//                if (confirm == JOptionPane.YES_OPTION && playerClient != null) {
//                    playerClient.sendResignation();
//                }
//            });
//            buttonPanel.add(resignButton);
//        }
//
//        JButton closeButton = new JButton("Close");
//        // FIX: Make close button do the same as X button
//        closeButton.addActionListener(e -> handleWindowClose());
//        buttonPanel.add(closeButton);
//
//        return buttonPanel;
//    }

    // NEW METHOD: Handle both X button and Close button the same way
    private void handleWindowClose() {
        System.out.println("Window close requested (X button or Close button)");

        if (playerClient != null) {
            // For players: trigger proper shutdown through PlayerClient
            playerClient.requestShutdown();
        } else {
            // For spectators or if no player client: direct shutdown
            System.out.println("No player client, exiting directly...");
            System.exit(0);
        }
    }

    private String getWindowTitle() {
        if (playerRole.equals("player")) {
            return "Player (" + playerColor + ")";
        } else {
            return "Spectator";
        }
    }

    public NetworkBoardPanel getBoardPanel() {
        return boardPanel;
    }

    // Set the player client for players (called after window creation)
    public void setPlayerClient(PlayerClient playerClient) {
        this.playerClient = playerClient;
        boardPanel.setPlayerClient(playerClient);
    }

    // Update the window with new game state
    public void updateGameState(GameState gameState) {
        SwingUtilities.invokeLater(() -> {
            // Update board
            boardPanel.updateFromGameState(gameState);

            // Update status labels
            if (gameState.isGameOver()) {
                if (gameState.getWinner() != null) {
                    statusLabel.setText("Game Over - " + gameState.getWinner() + " Wins!");
                    statusLabel.setForeground(Color.RED);
                } else {
                    statusLabel.setText("Game Over - Draw!");
                    statusLabel.setForeground(Color.BLUE);
                }
                turnLabel.setText("Game Finished");
            } else {
                statusLabel.setText("Game in Progress");
                statusLabel.setForeground(Color.BLACK);
                turnLabel.setText("Turn: " + (gameState.isWhiteTurn() ? "White" : "Black"));

                // Highlight if it's this player's turn
                if (playerRole.equals("player")) {
                    boolean isMyTurn = (playerColor.equals("white") && gameState.isWhiteTurn()) ||
                            (playerColor.equals("black") && !gameState.isWhiteTurn());
                    if (isMyTurn) {
                        turnLabel.setForeground(Color.GREEN);
                        turnLabel.setText("YOUR TURN (" + (gameState.isWhiteTurn() ? "White" : "Black") + ")");
                    } else {
                        turnLabel.setForeground(Color.BLACK);
                    }
                } else {
                    turnLabel.setForeground(Color.BLACK);
                }
            }

            moveCountLabel.setText("Move: " + gameState.getMoveCount());

            // Update window title if game is over
            if (gameState.isGameOver()) {
                gameWindow.setTitle("Chess - " + getWindowTitle() + " - GAME OVER");
            }
        });
    }

    // Show error message
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gameWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // Show info message
    public void showInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gameWindow, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void closeWindow() {
        SwingUtilities.invokeLater(() -> {
            gameWindow.dispose();
        });
    }

    public JFrame getFrame() {
        return gameWindow;
    }
}