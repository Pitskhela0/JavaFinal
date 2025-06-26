package ui;

import database.UserLoginResult;
import startMenu.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AuthWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public AuthWindow() {
        setTitle("Chess Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Welcome to Chess Multiplayer!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(buttonPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(this::handleLogin);
        registerBtn.addActionListener(this::handleRegister);

        setVisible(true);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        UserLoginResult result = AuthService.login(username, password);

        if (result != null) {
            dispose();
            ClientConnection clientConnection = new ClientConnection();
            clientConnection.setClientId(result.getUserId());
            clientConnection.setClientUsername(result.getUsername());
            clientConnection.start();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Invalid username or password");
        }
    }

    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        boolean success = AuthService.register(username, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "✅ Registered successfully! You can now log in.");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Username already taken.");
        }
    }
}
