package startMenu.menuStyling;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MenuStyles {

    // Color scheme - Chess themed
    public static final Color DARK_SQUARE = new Color(101, 67, 33);      // Dark brown
    public static final Color LIGHT_SQUARE = new Color(240, 217, 181);   // Light brown
    public static final Color ACCENT_COLOR = new Color(184, 134, 11);    // Gold
    public static final Color HOVER_COLOR = new Color(220, 158, 38);     // Light gold
    public static final Color TEXT_COLOR = new Color(255, 255, 255);     // White
    public static final Color BACKGROUND = new Color(45, 45, 45);        // Dark gray

    public static void styleButton(JButton button) {
        // MAKE ALL BUTTONS SAME WIDTH AND WIDER
        Dimension size = new Dimension(300, 50); // Increased width from 220 to 300
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        button.setBackground(DARK_SQUARE);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded corners
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(createRoundedBorder());

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(HOVER_COLOR);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(DARK_SQUARE);
                button.repaint();
            }
        });

        // Custom painting for rounded button
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded background
                g2.setColor(button.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);

                // Draw border
                g2.setColor(ACCENT_COLOR);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, c.getWidth()-2, c.getHeight()-2, 15, 15);

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    public static void styleField(JTextField textField) {
        Dimension dimension = new Dimension(300, 45); // Match button width
        textField.setPreferredSize(dimension);
        textField.setMinimumSize(dimension);
        textField.setMaximumSize(dimension);

        textField.setBackground(LIGHT_SQUARE);
        textField.setForeground(DARK_SQUARE);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(createFieldBorder());
        textField.setHorizontalAlignment(JTextField.CENTER);

        // Placeholder text effect
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBackground(Color.WHITE);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBackground(LIGHT_SQUARE);
            }
        });
    }

    public static void styleDropDown(JComboBox<String> comboBox) {
        Dimension dimension = new Dimension(300, 45); // Match button width
        comboBox.setPreferredSize(dimension);
        comboBox.setMinimumSize(dimension);
        comboBox.setMaximumSize(dimension);

        comboBox.setBackground(LIGHT_SQUARE);
        comboBox.setForeground(DARK_SQUARE);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(createFieldBorder());
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Custom renderer for dropdown items
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (isSelected) {
                    setBackground(ACCENT_COLOR);
                    setForeground(TEXT_COLOR);
                } else {
                    setBackground(LIGHT_SQUARE);
                    setForeground(DARK_SQUARE);
                }

                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return this;
            }
        });
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 32));
        label.setForeground(ACCENT_COLOR);

        // Simple text decoration instead of Unicode symbols
        String decoratedText = "*** " + text + " ***";
        label.setText(decoratedText);

        return label;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        label.setForeground(LIGHT_SQUARE);
        return label;
    }

    public static JPanel createChessPatternPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Paint chess pattern background
                int squareSize = 40;
                for (int row = 0; row < getHeight() / squareSize + 1; row++) {
                    for (int col = 0; col < getWidth() / squareSize + 1; col++) {
                        Color color = (row + col) % 2 == 0 ?
                                new Color(DARK_SQUARE.getRed(), DARK_SQUARE.getGreen(), DARK_SQUARE.getBlue(), 30) :
                                new Color(LIGHT_SQUARE.getRed(), LIGHT_SQUARE.getGreen(), LIGHT_SQUARE.getBlue(), 30);
                        g2.setColor(color);
                        g2.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
                    }
                }
            }
        };
    }

    private static Border createRoundedBorder() {
        return BorderFactory.createEmptyBorder(10, 20, 10, 20);
    }

    private static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }
}