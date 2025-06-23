package database;

import org.junit.jupiter.api.*;
import shared.ChessMove;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameDatabaseTest {

    private static final String TEST_DB_PATH = "test_chess.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_PATH;
    private GameDatabase db;

    @BeforeAll
    void setupSchema() throws SQLException {
        db = new GameDatabase() {
            @Override
            protected Connection getConnection() throws SQLException {
                return DriverManager.getConnection(TEST_DB_URL);
            }
        };

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    white_id INTEGER,
                    black_id INTEGER,
                    result TEXT,
                    status TEXT,
                    pgn_text TEXT,
                    end_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS moves (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_id INTEGER,
                    move_number INTEGER,
                    move_text TEXT,
                    FOREIGN KEY (game_id) REFERENCES games(id)
                );
            """);
        }
    }

    @AfterAll
    void cleanup() {
        File file = new File(TEST_DB_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testInsertGameWithMoves_andLoadPGN() {
        // Prepare dummy move list with overridden toChessNotation
        List<ChessMove> moves = new ArrayList<>();
        moves.add(new ChessMove(6, 4, 4, 4) { // e2 to e4
            @Override public String toChessNotation() { return "e4"; }
        });
        moves.add(new ChessMove(1, 4, 3, 4) { // e7 to e5
            @Override public String toChessNotation() { return "e5"; }
        });

        String expectedPGN = "1. e4 e5";
        db.insertGameWithMoves(1, 2, moves, expectedPGN);

        // Fetch the last inserted game ID
        int gameId = -1;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM games ORDER BY id DESC LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                gameId = rs.getInt("id");
            }
        } catch (SQLException e) {
            fail("Failed to fetch game ID: " + e.getMessage());
        }

        assertTrue(gameId > 0, "Game ID should be greater than zero");

        // Verify PGN
        String actualPGN = db.loadPGNByGameId(gameId);
        assertEquals(expectedPGN, actualPGN);
    }

    @Test
    void testLoadPGNByInvalidIdReturnsNull() {
        String result = db.loadPGNByGameId(-1);
        assertNull(result, "Should return null for invalid game ID");
    }
}
