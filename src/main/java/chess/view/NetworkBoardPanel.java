package chess.view;

import chess.model.BoardState;
import chess.model.Square;

import javax.swing.*;
import java.awt.*;

public class NetworkBoardPanel extends JPanel {
    private static final int TILE = 64;
    private final BoardState model;

    public NetworkBoardPanel() {
        this.model = new BoardState(); // Initialize with starting position
        int N = BoardState.SIZE;
        setLayout(new GridLayout(N, N));
        setPreferredSize(new Dimension(N * TILE, N * TILE));

        initializeBoard();
    }

    private void initializeBoard() {
        removeAll(); // Clear existing components
        Square[][] board = model.getSquareArray();
        for (int r = 0; r < BoardState.SIZE; r++) {
            for (int c = 0; c < BoardState.SIZE; c++) {
                add(new SquarePanel(board[r][c]));
            }
        }
        revalidate();
        repaint();
    }

    public BoardState getBoardState() {
        return model;
    }
}