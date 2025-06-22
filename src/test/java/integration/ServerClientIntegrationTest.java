package integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import serverSide.Server;
import clientSide.Client;
import startMenu.ClientConnection;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

public class ServerClientIntegrationTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testServerStartsAndAcceptsPort() throws Exception {
        Server server = new Server(1);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                // Expected when server shuts down
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Give server time to start
        Thread.sleep(1000);

        // Verify server assigned a port
        assertTrue(server.getPort() > 0);

        // Clean up
        server.endGame();
        serverThread.interrupt();
    }

    @Test
    public void testClientConnectionCreation() {
        assertDoesNotThrow(() -> {
            ClientConnection clientConnection = new ClientConnection();
            assertNotNull(clientConnection);
        });
    }
}