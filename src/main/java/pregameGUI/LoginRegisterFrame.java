package pregameGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginRegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginRegisterFrame(){
        setTitle("Login / Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);

        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        add(panel);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
    }

    private void login(){
        String email = usernameField.getText();
        String password = new String(passwordField.getPassword());
        System.out.println("Logging in with: " + email + " / " + password);
    }

    private void register(){
        String email = usernameField.getText();
        String password = new String(passwordField.getPassword());
        System.out.println("Registering in with: " + email + " / " + password);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginRegisterFrame frame = new LoginRegisterFrame();
            frame.setVisible(true);
        });
    }
}
