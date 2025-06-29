package startMenu.buttonFunctions;

import database.GameDatabase;
import parsing.GameParser;
import parsing.Move;
import parsing.Record;
import shared.ChessMove;
import startMenu.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameReviewWindow extends JFrame {
    private final ClientConnection clientConnection;

    public GameReviewWindow(JFrame previousWindow, ClientConnection clientConnection) {
        this.clientConnection = clientConnection;

        setTitle("Game Review - Select a Game");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(60, 60, 60));

        JLabel titleLabel = new JLabel("Your Saved Games", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> gameList = new JList<>(model);
        gameList.setFont(new Font("Monospaced", Font.BOLD, 16));
        JScrollPane scrollPane = new JScrollPane(gameList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        List<String> pgnList = GameDatabase.getPGNsByUser(clientConnection.getClientID());
        for (int i = 0; i < pgnList.size(); i++) {
            model.addElement("Game #" + (i + 1));
        }

        JButton replayButton = new JButton("Replay Selected Game");
        JButton importButton = new JButton("Import Game (PGN)");

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(60, 60, 60));
        bottomPanel.add(replayButton);
        bottomPanel.add(importButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        replayButton.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String pgn = pgnList.get(selectedIndex);
                launchReplay(pgn, previousWindow);
            }
        });

        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File pgnFile = fileChooser.getSelectedFile();
                try {
                    String pgnText = new String(Files.readAllBytes(pgnFile.toPath()));
                    Record record = GameParser.parsePGN(pgnText);
                    if (record == null) {
                        JOptionPane.showMessageDialog(this, "Could not parse PGN file.");
                        return;
                    }
                    String white = record.getWhite();
                    String black = record.getBlack();
                    String currentUsername = getUsernameById(String.valueOf(clientConnection.getClientID()));

                    if (currentUsername.equals(white) || currentUsername.equals(black)) {
                        GameDatabase.insertGamePGN(clientConnection.getClientID(), pgnText);
                        JOptionPane.showMessageDialog(this, "Game imported to your list.");
                        model.addElement("Game #" + (model.size() + 1));
                        pgnList.add(pgnText);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "This game is not played by you. Replaying it temporarily.");
                    }

                    List<Move> parsedMoves = record.getMoves();
                    List<ChessMove> moves = convertToChessMoves(parsedMoves);
                    new ReplayWindowWithBoard(moves, this, white, black);
                    this.setVisible(false);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load PGN: " + ex.getMessage());
                }
            }
        });

        setContentPane(mainPanel);
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                previousWindow.setVisible(true);
            }
        });
    }

    private String getUsernameById(String id) {
        String username = "Unknown";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:chess.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }

    private void launchReplay(String pgn, JFrame previousWindow) {
        Record record = GameParser.parsePGN(pgn);
        if (record == null) {
            JOptionPane.showMessageDialog(this, "PGN could not be parsed!");
            return;
        }
        List<Move> parsedMoves = record.getMoves();
        List<ChessMove> moves = convertToChessMoves(parsedMoves);
        SwingUtilities.invokeLater(() -> new ReplayWindowWithBoard(moves, this, getUsernameById(record.getWhite()), getUsernameById(record.getBlack())));
        this.setVisible(false);
    }

    public static List<ChessMove> convertToChessMoves(List<parsing.Move> parsedMoves) {
        List<ChessMove> result = new ArrayList<>();
        for (parsing.Move move : parsedMoves) {
            if (move == null || move.getAction() == null || move.getAction().length() < 4) continue;
            String uci = move.getAction();
            char fromFile = uci.charAt(0);
            int fromRank = Character.getNumericValue(uci.charAt(1));
            char toFile = uci.charAt(2);
            int toRank = Character.getNumericValue(uci.charAt(3));
            int fromRow = 8 - fromRank;
            int fromCol = fromFile - 'a';
            int toRow = 8 - toRank;
            int toCol = toFile - 'a';
            result.add(new ChessMove(fromRow, fromCol, toRow, toCol));
        }
        return result;
    }
}
