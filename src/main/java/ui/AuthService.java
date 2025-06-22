package ui;

import database.DatabaseManager;
import database.UserService;

import java.sql.Connection;
import java.sql.DriverManager;

public class AuthService {
    private static final String DB_URL = "jdbc:sqlite:chess.db"; // persistent file

    public static boolean login(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            DatabaseManager.initializeSchema(conn);
            return UserService.login(conn, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean register(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            DatabaseManager.initializeSchema(conn);
            return UserService.register(conn, username, password, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
