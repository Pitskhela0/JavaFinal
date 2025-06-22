package startMenu.buttonFunctions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import startMenu.ClientConnection;
import startMenu.buttonFunctions.PlayWithBotButton;

import static org.junit.jupiter.api.Assertions.*;

public class PlayWithBotButtonTest {

    private ClientConnection clientConnection;
    private PlayWithBotButton playWithBotButton;

    @BeforeEach
    public void setUp() {
        clientConnection = new ClientConnection();
        playWithBotButton = new PlayWithBotButton(clientConnection);
    }

    @Test
    public void testPlayWithBotButtonCreation() {
        assertNotNull(playWithBotButton);
    }

    @Test
    public void testPlayWithBotButtonCreationDoesNotThrow() {
        // Test that we can create the button without errors
        assertDoesNotThrow(() -> {
            new PlayWithBotButton(new ClientConnection());
        });
    }

    @Test
    public void testMultipleButtonCreation() {
        // Test creating multiple buttons doesn't cause issues
        PlayWithBotButton button1 = new PlayWithBotButton(new ClientConnection());
        PlayWithBotButton button2 = new PlayWithBotButton(new ClientConnection());

        assertNotNull(button1);
        assertNotNull(button2);
        assertNotSame(button1, button2);
    }
}