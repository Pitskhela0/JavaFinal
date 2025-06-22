package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    public static void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    email TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    white_id INTEGER,
                    black_id INTEGER,
                    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    end_time TIMESTAMP,
                    result TEXT,
                    status TEXT,
                    pgn_text TEXT,
                    FOREIGN KEY (white_id) REFERENCES users(id),
                    FOREIGN KEY (black_id) REFERENCES users(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS moves (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_id INTEGER NOT NULL,
                    move_number INTEGER NOT NULL,
                    from_row INTEGER,
                    from_col INTEGER,
                    to_row INTEGER,
                    to_col INTEGER,
                    special_move TEXT,
                    move_type TEXT,
                    timestamp TIMESTAMP,
                    FOREIGN KEY (game_id) REFERENCES games(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS spectators (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    game_id INTEGER NOT NULL,
                    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (game_id) REFERENCES games(id)
                );
            """);
        }
    }
}
