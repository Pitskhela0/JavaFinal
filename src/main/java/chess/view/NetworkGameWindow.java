package chess.view;

import javax.swing.*;
import java.awt.*;

public class NetworkGameWindow {
    private final JFrame gameWindow;
    private final NetworkBoardPanel boardPanel;
    private final String playerRole;
    private final String playerColor; // For players: "white", "black", for spectators: "spectator"

    public NetworkGameWindow(String playerRole, String playerColor) {
        this.playerRole = playerRole;
        this.playerColor = playerColor;

        gameWindow = new JFrame("Chess - " + getWindowTitle());
        gameWindow.setLayout(new BorderLayout());
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create board panel
        boardPanel = new NetworkBoardPanel();

        // Create info panel
        JPanel infoPanel = new JPanel();
        JLabel roleLabel = new JLabel("Role: " + playerRole +
                (playerRole.equals("player") ? " (" + playerColor + ")" : ""));
        infoPanel.add(roleLabel);

        gameWindow.add(infoPanel, BorderLayout.NORTH);
        gameWindow.add(boardPanel, BorderLayout.CENTER);

        gameWindow.pack();
        gameWindow.setResizable(false);
        gameWindow.setVisible(true);
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

    public void closeWindow() {
        gameWindow.dispose();
    }
}