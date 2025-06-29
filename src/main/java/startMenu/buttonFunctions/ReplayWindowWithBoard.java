package startMenu.buttonFunctions;

import chess.view.BoardPanel;
import shared.ChessMove;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ReplayWindowWithBoard extends JFrame {
    private final BoardPanel boardPanel;
    private final List<ChessMove> moves;
    private int moveIndex = 0;
    private final String whitePlayer;
    private final String blackPlayer;

    public ReplayWindowWithBoard(List<ChessMove> moves, JFrame previousWindow, String whitePlayer, String blackPlayer) {
        this.moves = moves;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;

        setUndecorated(true);
        setSize(750, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Chess background pattern
        setContentPane(new ChessBackgroundPanel());
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 4));

        // Top Panel: Players + Close X
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel playersLabel = new JLabel("♔ " + whitePlayer + " vs ♚ " + blackPlayer, SwingConstants.CENTER);
        playersLabel.setFont(new Font("Serif", Font.BOLD, 18));
        playersLabel.setForeground(Color.WHITE);
        playersLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel closeLabel = new JLabel("✕");
        closeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        closeLabel.setForeground(Color.LIGHT_GRAY);
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                previousWindow.setVisible(true);
            }
        });

        topPanel.add(playersLabel, BorderLayout.CENTER);
        topPanel.add(closeLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: Board
        boardPanel = new BoardPanel(null);
        add(boardPanel, BorderLayout.CENTER);

        // Bottom Panel: Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controls.setOpaque(false);

        JButton prev = new JButton("PREVIOUS");
        JButton next = new JButton("NEXT");
        styleButton(prev);
        styleButton(next);

        prev.addActionListener(e -> {
            if (moveIndex > 0) {
                moveIndex--;
                boardPanel.resetBoard();
                for (int i = 0; i < moveIndex; i++) {
                    boardPanel.applyMove(moves.get(i));
                }
            }
        });

        next.addActionListener(e -> {
            if (moveIndex < moves.size()) {
                boardPanel.applyMove(moves.get(moveIndex++));
            }
        });

        controls.add(prev);
        controls.add(next);
        add(controls, BorderLayout.SOUTH);

        // Restore previous window on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                previousWindow.setVisible(true);
            }
        });

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(153, 101, 21));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Serif", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    // Inner class for custom chess-style background
    static class ChessBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int tileSize = 40;
            Color dark = new Color(64, 64, 64);
            Color light = new Color(90, 90, 90);
            for (int row = 0; row < getHeight() / tileSize + 1; row++) {
                for (int col = 0; col < getWidth() / tileSize + 1; col++) {
                    g.setColor((row + col) % 2 == 0 ? dark : light);
                    g.fillRect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }
    }
}
