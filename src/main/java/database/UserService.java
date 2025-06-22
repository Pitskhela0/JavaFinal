package database;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserService {

    public static boolean register(Connection conn, String username, String password, String email) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.setString(3, email);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean login(Connection conn, String username, String password) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT password_hash FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return BCrypt.checkpw(password, rs.getString("password_hash"));
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    public static int getUserId(Connection conn, String username) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            return -1;
        }
        return -1;
    }
}
