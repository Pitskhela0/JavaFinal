package chess.app;

import chess.view.GameWindow;
import chess.view.StartMenu;

import javax.swing.*;

public class Game implements Runnable {
    @Override
    public void run() {
        SwingUtilities.invokeLater(
                () ->
                new GameWindow("Black Player", "White Player", 1,0,0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Game());
    }
}
