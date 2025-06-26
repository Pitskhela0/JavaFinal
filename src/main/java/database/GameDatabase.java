package database;

import shared.ChessMove;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDatabase {
    private static final String DB_URL = "jdbc:sqlite:chess.db";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void insertGameWithMoves(int whiteId, int blackId, List<ChessMove> moves, String pgn) {
        try (Connection conn = getConnection()) {
            String gameSql = """
            INSERT INTO games (white_id, black_id, result, status, pgn_text, end_time)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
            int generatedGameId = -1;

            try (PreparedStatement stmt = conn.prepareStatement(gameSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, whiteId);
                stmt.setInt(2, blackId);
                stmt.setString(3, "1-0"); // or use actual result
                stmt.setString(4, "finished");
                stmt.setString(5, pgn);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedGameId = rs.getInt(1);
                }
            }

            String moveSql = "INSERT INTO moves (game_id, move_number, move_text) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(moveSql)) {
                for (int i = 0; i < moves.size(); i++) {
                    stmt.setInt(1, generatedGameId);
                    stmt.setInt(2, i + 1);
                    stmt.setString(3, moves.get(i).toChessNotation());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String loadPGNByGameId(int gameId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT pgn_text FROM games WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gameId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("pgn_text");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getPGNsByUser(int userId) {
        List<String> pgns = new ArrayList<>();
        String query = "SELECT pgn_text FROM games WHERE white_id = ? OR black_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:chess.db");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String pgn = rs.getString("pgn_text");
                if (pgn != null && !pgn.isBlank()) {
                    pgns.add(pgn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pgns;
    }
}
