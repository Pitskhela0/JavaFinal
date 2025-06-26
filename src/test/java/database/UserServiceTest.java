package database;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private Connection conn;

    @BeforeAll
    void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite:chess.db");
        DatabaseManager.initializeSchema(conn);
    }

    @BeforeEach
    void clearUsers() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users;");
        }
    }

    @Test
    void testRegisterNewUser() {
        assertTrue(UserService.register(conn, "alice", "pass123", "alice@mail.com"));
    }

    @Test
    void testDuplicateUsernameFails() {
        UserService.register(conn, "bob", "pass", "bob@mail.com");
        assertFalse(UserService.register(conn, "bob", "newpass", "bob2@mail.com"));
    }

    @Test
    void testLoginSuccess() {
        UserService.register(conn, "carol", "secret", "carol@mail.com");
        assertTrue(UserService.login(conn, "carol", "secret") != null);
    }

    @Test
    void testLoginFailsWrongPassword() {
        UserService.register(conn, "dave", "secret", "dave@mail.com");
        assertFalse(UserService.login(conn, "dave", "wrongpass") != null);
    }

    @Test
    void testLoginFailsNoUser() {
        assertFalse(UserService.login(conn, "ghost", "nopass") != null);
    }

    @Test
    void testGetUserIdFound() {
        assertTrue(UserService.register(conn, "eve", "123", "eve@mail.com"));
        int id = UserService.getUserId(conn, "eve");
        assertTrue(id > 0, "❌ User ID was not greater than 0");
    }

    @Test
    void testGetUserIdNotFound() {
        assertEquals(-1, UserService.getUserId(conn, "unknown"));
    }
}
