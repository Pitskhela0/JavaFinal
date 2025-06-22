package startMenu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClientConnectionTest {

    private ClientConnection clientConnection;

    @BeforeEach
    public void setUp() {
        clientConnection = new ClientConnection();
    }

    @Test
    public void testClientConnectionInitialization() {
        assertNotNull(clientConnection);
        assertFalse(clientConnection.isHosting()); // Should start as not hosting
    }

    @Test
    public void testHostingStateChange() {
        assertFalse(clientConnection.isHosting());

        // Note: hostGame() changes the hosting state and starts threads
        // For unit testing, we'd need to refactor to make this more testable
        // This test just verifies initial state
    }

    @Test
    public void testStopCurrentServerDoesNotThrow() {
        // Should handle gracefully even when no server is running
        assertDoesNotThrow(() -> {
            clientConnection.stopCurrentServer();
        });
    }

    @Test
    public void testDisposeDoesNotThrow() {
        assertDoesNotThrow(() -> {
            clientConnection.dispose();
        });
    }

    @Test
    public void testShowStartMenuDoesNotThrow() {
        assertDoesNotThrow(() -> {
            clientConnection.showStartMenu();
        });
    }
}