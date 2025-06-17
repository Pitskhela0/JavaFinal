package chess.view;

import chess.model.Square;
import chess.model.pieces.Piece;
import javax.swing.*;
import java.awt.*;

public class SquarePanel extends JPanel {
    private final Square squareData;
    private boolean displayPiece = true;

    public SquarePanel(Square squareData) {
        this.squareData = squareData;
        setPreferredSize(new Dimension(64, 64));
        setOpaque(true);
    }

    public Square getSquareData() {
        return squareData;
    }

    public Piece getOccupyingPiece() {
        return squareData.getOccupyingPiece();
    }

    public void setDisplayPiece(boolean display) {
        this.displayPiece = display;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color bg = (squareData.getColor() == 1)
                ? new Color(221, 192, 127)
                : new Color(101,  67,  33);
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (displayPiece) {
            Piece p = squareData.getOccupyingPiece();
            if (p != null) {
                Image img = p.getImage();
                if (img != null) {
                    g.drawImage(img,
                            0, 0,
                            getWidth(), getHeight(),
                            this);
                }
            }
        }
    }
}