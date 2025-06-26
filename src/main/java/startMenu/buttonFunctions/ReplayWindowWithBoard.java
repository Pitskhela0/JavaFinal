package startMenu.buttonFunctions;

import chess.view.BoardPanel;
import shared.ChessMove;

import javax.swing.*;
import java.awt.*;
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
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
        getContentPane().setBackground(new Color(40, 40, 40));

        setTitle("Replay Game (Real Board)");
        setSize(700, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        boardPanel = new BoardPanel(null); // null TimerStarter for replay
        add(boardPanel, BorderLayout.CENTER);

        JLabel playersLabel = new JLabel("♔ " + whitePlayer + " vs ♚ " + blackPlayer, SwingConstants.CENTER);
        playersLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        playersLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(playersLabel, BorderLayout.NORTH);

        JPanel controls = new JPanel();
        JButton next = new JButton("NEXT");
        styleButton(next);
        JButton prev = new JButton("PREVIOUS");
        styleButton(prev);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            dispose();
            previousWindow.setVisible(true);
        });
        controls.add(closeBtn);

        controls.add(prev);
        controls.add(next);
        add(controls, BorderLayout.SOUTH);

        next.addActionListener(e -> {
            if (moveIndex < moves.size()) {
                boardPanel.applyMove(moves.get(moveIndex++));
            }
        });

        prev.addActionListener(e -> {
            if (moveIndex > 0) {
                moveIndex--;
                boardPanel.resetBoard();
                for (int i = 0; i < moveIndex; i++) {
                    boardPanel.applyMove(moves.get(i));
                }
            }
        });

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
        button.setFont(new Font("Serif", Font.BOLD, 18));
    }
}
