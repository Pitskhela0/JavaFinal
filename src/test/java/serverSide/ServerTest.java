package serverSide;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ServerTest {

    private Server server;

    @BeforeEach
    public void setUp() {
        server = new Server(1);
    }

    @Test
    public void testServerInitialization() {
        assertNotNull(server);
        assertFalse(server.isGameStarted());
        assertNotNull(server.getPlayers());
        assertNotNull(server.getSpectators());
        assertTrue(server.getPlayers().isEmpty());
        assertTrue(server.getSpectators().isEmpty());
    }

    @Test
    public void testGameStartedFlag() {
        assertFalse(server.isGameStarted());

        server.setGameStarted(true);
        assertTrue(server.isGameStarted());

        server.setGameStarted(false);
        assertFalse(server.isGameStarted());
    }

    @Test
    public void testPlayerListManagement() {
        List<ClientHandler> players = server.getPlayers();
        assertTrue(players.isEmpty());

        // Test that we get the same reference (important for synchronization)
        assertSame(players, server.getPlayers());
    }

    @Test
    public void testSpectatorListManagement() {
        List<ClientHandler> spectators = server.getSpectators();
        assertTrue(spectators.isEmpty());

        // Test that we get the same reference
        assertSame(spectators, server.getSpectators());
    }

    @Test
    public void testBoardStateInitialization() {
        assertNull(server.getBoard()); // Board should be null until game starts
    }
}