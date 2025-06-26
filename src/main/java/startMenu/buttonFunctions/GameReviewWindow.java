package startMenu.buttonFunctions;

import database.GameDatabase;
import parsing.GameParser;
import parsing.Move;
import parsing.Record;
import shared.ChessMove;
import shared.GameState;
import startMenu.ClientConnection;

import javax.swing.*;
import java.awt.*;
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

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> gameList = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(gameList);
        add(scrollPane, BorderLayout.CENTER);

        List<String> pgnList = GameDatabase.getPGNsByUser(clientConnection.getClientID());
        for (int i = 0; i < pgnList.size(); i++) {
            model.addElement("Game #" + (i + 1));
        }

        JButton replayButton = new JButton("Replay Selected Game");
        add(replayButton, BorderLayout.SOUTH);

        replayButton.addActionListener(e -> {
            int selectedIndex = gameList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String pgn = pgnList.get(selectedIndex);

                System.out.println("PGN from DB: " + pgn);

                Record record = GameParser.parsePGN(pgn);
                if (record == null) {
                    JOptionPane.showMessageDialog(this, "PGN could not be parsed!");
                    return;
                }
                List<Move> parsedMoves = record.getMoves();
                List<ChessMove> moves = convertToChessMoves(parsedMoves);
                SwingUtilities.invokeLater(() -> new ReplayWindowWithBoard(moves, this, getUsernameById(record.getWhite()), getUsernameById(record.getBlack())));

            }
        });

        // Show and handle close
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                previousWindow.setVisible(true); // Re-open main menu
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
