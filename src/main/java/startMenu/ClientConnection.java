package startMenu;

import serverSide.Server;
import startMenu.buttonFunctions.*;
import startMenu.buttonFunctions.PlayWithBotButton;
import startMenu.menuStyling.MenuStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    private record Result(JButton hostGameButton, JButton joinGameButton,
                          JButton spectateGameButton, JButton watchGameFromDBButton,
                          JButton playWithBotButton) {
    }

    public void setClientId(int userId) {
        clientID = userId;
    }

    public int getClientID() {
        return this.clientID;
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

    private Result getResult() {
        setTitle("Chess Master - Game Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650); // Slightly taller for better spacing
        setLocationRelativeTo(null);
        setResizable(false);

        // Create main panel with chess pattern background
        JPanel mainPanel = MenuStyles.createChessPatternPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(MenuStyles.BACKGROUND);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        // Title
        JLabel titleLabel = MenuStyles.createTitleLabel("Chess Master");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = MenuStyles.createSubtitleLabel("Choose your game mode");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subtitleLabel);

        // Content panel - USING VERTICAL LAYOUT FOR CONSISTENT WIDTH
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        // Create styled components
        joiningIDField = new JTextField("Enter Game ID");
        spectatingIDField = new JTextField("Enter Game ID to Spectate");

        MenuStyles.styleField(joiningIDField);
        MenuStyles.styleField(spectatingIDField);

        // Add placeholder behavior
        addPlaceholderBehavior(joiningIDField, "Enter Game ID");
        addPlaceholderBehavior(spectatingIDField, "Enter Game ID to Spectate");

        JButton hostGameButton = createStyledButton("HOST NEW GAME", "Start a new game and invite friends");
        JButton joinGameButton = createStyledButton("JOIN GAME", "Join an existing game");
        JButton spectateGameButton = createStyledButton("SPECTATE GAME", "Watch a game in progress");
        JButton watchGameFromDBButton = createStyledButton("REVIEW GAME", "Watch previously played games");
        JButton playWithBotButton = createStyledButton("PLAY VS BOT", "Challenge the computer");

        String[] gameList = {"Championship Final", "Epic Battle", "Quick Match"};
        JComboBox<String> gameDropdown = new JComboBox<>(gameList);
        MenuStyles.styleDropDown(gameDropdown);

        // Add all components with consistent spacing and centering
        contentPanel.add(createCenteredComponent(hostGameButton));
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createCenteredComponent(joinGameButton));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createCenteredComponent(spectateGameButton));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createCenteredComponent(watchGameFromDBButton));
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createCenteredComponent(gameDropdown));
        contentPanel.add(Box.createVerticalStrut(15));

        contentPanel.add(createCenteredComponent(playWithBotButton));

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("May the best player win!");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footerLabel.setForeground(MenuStyles.LIGHT_SQUARE);
        footerPanel.add(footerLabel);

        // Assemble main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        return new Result(hostGameButton, joinGameButton, spectateGameButton, watchGameFromDBButton, playWithBotButton);
    }

    // Helper method to center components
    private JPanel createCenteredComponent(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);
        panel.add(component);
        return panel;
    }

    private void addPlaceholderBehavior(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(MenuStyles.DARK_SQUARE);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        MenuStyles.styleButton(button);
        button.setToolTipText(tooltip);
        return button;
    }
}