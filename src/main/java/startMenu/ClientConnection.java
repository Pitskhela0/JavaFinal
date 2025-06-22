package startMenu;

import serverSide.Server;
import startMenu.buttonFunctions.*;
import startMenu.buttonFunctions.PlayWithBotButton;
import startMenu.menuStyling.MenuStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ClientConnection extends JFrame{
    private boolean isHosting = false;
    private int clientID;
    private String clientName;
    private String email;
    private JTextField joiningIDField;
    private JTextField spectatingIDField;

    // Track server thread to properly shut it down
    private Thread currentServerThread;
    private Server currentServer;

    public boolean isHosting() {
        return isHosting;
    }

    public ClientConnection(){

    }

    public void showStartMenu() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront(); // Bring window to front
            requestFocus(); // Give it focus
        });
    }

    public void start(){
        // main menu logic
        // render main menu with diff buttons, hostGame, joinGame, watchPlayedGame
        Result result = getResult();

        result.hostGameButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostGame();
            }
        });

        result.joinGameButton().addActionListener(e -> joinGame(joiningIDField));

        result.spectateGameButton().addActionListener(e -> spectateGame(spectatingIDField));

        result.watchGameFromDBButton().addActionListener(e -> watchGameFromDB());

        result.playWithBotButton().addActionListener(e -> playWithBot());


        setVisible(true);
    }

    public void hostGame(){
        isHosting = true;
        HostGameButton game = new HostGameButton(this);
        game.hostGame();
    }

    public void joinGame(JTextField gameIDText){
        // find server with entered gameID and connect to its port as client
        isHosting = false;
        JoinGamePlayerButton blackPlayerHandler = new JoinGamePlayerButton(this,gameIDText);
        blackPlayerHandler.joinGame();
    }

    public void spectateGame(JTextField gameID){
        // find server with entered gameID and connect to its port as spectator
        isHosting = false;
        SpectateGameButton spectator = new SpectateGameButton(this, gameID);
        spectator.spectate();
    }

    public void watchGameFromDB(){
        // retrieve game from db, run auto-game
        isHosting = false;
        WatchGameFromDBButton dbWatching = new WatchGameFromDBButton(this);
        dbWatching.startWatching();
    }

    public void playWithBot(){
        isHosting = true;
        PlayWithBotButton botPlaying = new PlayWithBotButton(this);
        botPlaying.hostGameWithBot();

    }

    // Override window closing to clean up server
    @Override
    public void dispose() {
        stopCurrentServer();
        super.dispose();
    }

    private Result getResult() {
        setTitle("Chess Game - Start Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        joiningIDField = new JTextField(20);
        spectatingIDField = new JTextField(20);

        MenuStyles.styleField(joiningIDField);
        MenuStyles.styleField(spectatingIDField);

        JButton hostGameButton = new JButton("Host Game");
        JButton joinGameButton = new JButton("Join Game");
        JButton spectateGameButton = new JButton("Spectate Game");
        JButton watchGameFromDBButton = new JButton("Review Played Game");
        JButton playWithBotButton = new JButton("Play with bot");

        MenuStyles.styleButton(hostGameButton);
        MenuStyles.styleButton(joinGameButton);
        MenuStyles.styleButton(spectateGameButton);
        MenuStyles.styleButton(watchGameFromDBButton);
        MenuStyles.styleButton(playWithBotButton);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Host Game button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(hostGameButton, gbc);
        // reset width
        gbc.gridwidth = 1;

        // Join Game Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(joinGameButton, gbc);

        // Join Game Field (right of label, same row)
        gbc.gridx = 1;
        panel.add(joiningIDField, gbc);

        // spectate game button
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(spectateGameButton,gbc);

        // spectate game field for game id
        gbc.gridx = 1;
        panel.add(spectatingIDField,gbc);


        String[] gameList = { "Game #101", "Game #102", "Game #103" };
        JComboBox<String> gameDropdown = new JComboBox<>(gameList);

        MenuStyles.styleDropDown(gameDropdown);


        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(watchGameFromDBButton,gbc);

        gbc.gridx = 1;
        panel.add(gameDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(playWithBotButton, gbc);

        add(panel);
        return new Result(hostGameButton, joinGameButton, spectateGameButton, watchGameFromDBButton, playWithBotButton);
    }

    private record Result(JButton hostGameButton, JButton joinGameButton,
                          JButton spectateGameButton, JButton watchGameFromDBButton,
                          JButton playWithBotButton) {
    }
    // Method to properly stop the current server
    public void stopCurrentServer() {
        System.out.println("Stopping current server if running...");

        if (currentServerThread != null && currentServerThread.isAlive()) {
            System.out.println("Interrupting existing server thread...");

            // Force end the current game to close server
            if (currentServer != null) {
                try {
                    // Call endGame to close server socket
                    currentServer.endGame();
                    Thread.sleep(1000); // Wait for cleanup
                } catch (Exception e) {
                    System.err.println("Error stopping server: " + e.getMessage());
                }
            }

            // Interrupt the thread
            currentServerThread.interrupt();

            // Wait for thread to die
            try {
                currentServerThread.join(3000); // Wait up to 3 seconds
                if (currentServerThread.isAlive()) {
                    System.err.println("Warning: Server thread did not stop gracefully");
                } else {
                    System.out.println("Server thread stopped successfully");
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for server thread to stop");
            }
        }

        // Clear references
        currentServerThread = null;
        currentServer = null;

        // Additional cleanup - wait a bit more for port to be released
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}