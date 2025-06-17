package chess.view;

import javax.swing.*;
import java.awt.*;

public class StartMenu implements Runnable {
    @Override
    public void run() {
        JFrame startWindow = new JFrame("Chess Start Menu");
        startWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startWindow.setSize(300, 350);
        startWindow.setLocationRelativeTo(null);
        startWindow.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Chess Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel namesPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        namesPanel.setMaximumSize(new Dimension(280, 60));
        namesPanel.add(new JLabel("Black:", SwingConstants.RIGHT));
        JTextField blackNameField = new JTextField("Black");
        namesPanel.add(blackNameField);
        namesPanel.add(new JLabel("White:", SwingConstants.RIGHT));
        JTextField whiteNameField = new JTextField("White");
        namesPanel.add(whiteNameField);
        mainPanel.add(namesPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new GridLayout(3, 3, 5, 5));

        JLabel hourLabel = new JLabel("Hours:");
        JLabel minuteLabel = new JLabel("Minutes:");
        JLabel secondLabel = new JLabel("Seconds:");

        JTextField hourField = new JTextField("0", 3);
        JTextField minuteField = new JTextField("2", 3);
        JTextField secondField = new JTextField("0", 3);

        JButton hourInc = new JButton("+");
        JButton hourDec = new JButton("-");
        JButton minuteInc = new JButton("+");
        JButton minuteDec = new JButton("-");
        JButton secondInc = new JButton("+");
        JButton secondDec = new JButton("-");

        hourInc.addActionListener(e -> {
            int val = Integer.parseInt(hourField.getText());
            hourField.setText(String.valueOf(val + 1));
        });
        hourDec.addActionListener(e -> {
            int val = Integer.parseInt(hourField.getText());
            if (val > 0) hourField.setText(String.valueOf(val - 1));
        });
        minuteInc.addActionListener(e -> {
            int val = Integer.parseInt(minuteField.getText());
            minuteField.setText(String.valueOf(val + 1));
        });
        minuteDec.addActionListener(e -> {
            int val = Integer.parseInt(minuteField.getText());
            if (val > 0) minuteField.setText(String.valueOf(val - 1));
        });
        secondInc.addActionListener(e -> {
            int val = Integer.parseInt(secondField.getText());
            secondField.setText(String.valueOf(val + 1));
        });
        secondDec.addActionListener(e -> {
            int val = Integer.parseInt(secondField.getText());
            if (val > 0) secondField.setText(String.valueOf(val - 1));
        });

        timerPanel.add(hourLabel);
        timerPanel.add(hourField);
        JPanel hourBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hourBtnPanel.add(hourDec);
        hourBtnPanel.add(hourInc);
        timerPanel.add(hourBtnPanel);

        timerPanel.add(minuteLabel);
        timerPanel.add(minuteField);
        JPanel minuteBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        minuteBtnPanel.add(minuteDec);
        minuteBtnPanel.add(minuteInc);
        timerPanel.add(minuteBtnPanel);

        timerPanel.add(secondLabel);
        timerPanel.add(secondField);
        JPanel secondBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        secondBtnPanel.add(secondDec);
        secondBtnPanel.add(secondInc);
        timerPanel.add(secondBtnPanel);

        mainPanel.add(timerPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton instructButton = new JButton("Instructions");
        JButton quitButton = new JButton("Quit");
        buttonPanel.add(startButton);
        buttonPanel.add(instructButton);
        buttonPanel.add(quitButton);
        mainPanel.add(buttonPanel);

        startButton.addActionListener(e -> {
            String bn = blackNameField.getText();
            String wn = whiteNameField.getText();
            int hh = Integer.parseInt(hourField.getText());
            int mm = Integer.parseInt(minuteField.getText());
            int ss = Integer.parseInt(secondField.getText());
            new GameWindow(bn, wn, hh, mm, ss);
            startWindow.dispose();
        });
        instructButton.addActionListener(e -> JOptionPane.showMessageDialog(startWindow,
                "Enter player names and adjust the timer using the +/- buttons.\nDefault is 0:2:0 (2 minutes).\nThen click 'Start' to begin.",
                "Instructions", JOptionPane.INFORMATION_MESSAGE));
        quitButton.addActionListener(e -> startWindow.dispose());

        startWindow.add(mainPanel);
        startWindow.setVisible(true);
    }
}