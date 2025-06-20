package startMenu.menyStyling;

import javax.swing.*;
import java.awt.*;

public class MenuStyles {
    public static void styleButton(JButton button) {
        Dimension size = new Dimension(180, 40);  // Set same size for all buttons
        button.setPreferredSize(size);
        button.setBackground(new Color(203, 68, 106, 255)); // Steel blue
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    public static void styleField(JTextField textField){
        Dimension dimension = new Dimension(200, 38);
        textField.setPreferredSize(dimension);
    }

    public static void styleDropDown(JComboBox<String> jComboBox){
        Dimension dimension = new Dimension(200, 38);
        jComboBox.setPreferredSize(dimension);
        jComboBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }
}
