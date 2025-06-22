package ui;

import database.DatabaseManager;
import database.UserService;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private static final String DB_URL = "jdbc:sqlite:chess.db";

    @BeforeEach
    void clearUsersTable() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
            DatabaseManager.initializeSchema(conn);
        }
    }

    @Test
    void testRegisterNewUser() {
        boolean result = AuthService.register("testuser", "password123");
        assertTrue(result, "User should be registered");
    }

    @Test
    void testRegisterDuplicateUser() {
        AuthService.register("dupeuser", "pass1");
        boolean result = AuthService.register("dupeuser", "pass2");
        assertFalse(result, "Duplicate registration should fail");
    }

    @Test
    void testLoginSuccess() {
        AuthService.register("loginuser", "mypassword");
        boolean result = AuthService.login("loginuser", "mypassword");
        assertTrue(result, "Login should succeed");
    }

    @Test
    void testLoginFailure() {
        AuthService.register("loginfail", "correctpass");
        boolean result = AuthService.login("loginfail", "wrongpass");
        assertFalse(result, "Login with wrong password should fail");
    }
}
