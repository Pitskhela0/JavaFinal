package startMenu.buttonFunctions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import startMenu.ClientConnection;
import startMenu.buttonFunctions.HostGameButton;

import static org.junit.jupiter.api.Assertions.*;

public class HostGameButtonTest {

    private ClientConnection clientConnection;
    private HostGameButton hostGameButton;

    @BeforeEach
    public void setUp() {
        clientConnection = new ClientConnection();
        hostGameButton = new HostGameButton(clientConnection);
    }

    @Test
    public void testHostGameButtonCreation() {
        assertNotNull(hostGameButton);
        assertEquals(0, hostGameButton.getGAME_ID()); // Default game ID should be 0
    }

    @Test
    public void testHostGameButtonCreationDoesNotThrow() {
        // Test that we can create the button without errors
        assertDoesNotThrow(() -> {
            new HostGameButton(new ClientConnection());
        });
    }

    @Test
    public void testGameIDGeneration() {
        HostGameButton button1 = new HostGameButton(new ClientConnection());
        HostGameButton button2 = new HostGameButton(new ClientConnection());

        // Both should generate the same ID (0) since gameIDGenerator() returns 0
        assertEquals(button1.getGAME_ID(), button2.getGAME_ID());
    }
}