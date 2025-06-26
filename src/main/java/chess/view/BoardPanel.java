package chess.view;

import chess.model.BoardState;
import chess.model.Square;
import chess.model.pieces.*;
import shared.ChessMove;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener {
    private static final int TILE = 64;
    final BoardState model;
    private final TimerStarter timerStarter;
    Piece draggingPiece;
    Square sourceSquare;
    SquarePanel sourcePanel;
    boolean dragging;
    boolean ghostActive;
    private int dragX, dragY;

    public BoardPanel(TimerStarter starter) {
        this.model = new BoardState();
        this.timerStarter = starter;
        int N = BoardState.SIZE;
        setLayout(new GridLayout(N, N));
        setPreferredSize(new Dimension(N * TILE, N * TILE));
        Square[][] board = model.getSquareArray();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                add(new SquarePanel(board[r][c]));
            }
        }
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (ghostActive && draggingPiece != null) {
            g.drawImage(draggingPiece.getImage(), dragX - TILE/2, dragY - TILE/2, TILE, TILE, null);
        }
    }

    SquarePanel panelAt(int x, int y) {
        Component comp = getComponentAt(x, y);
        return (comp instanceof SquarePanel) ? (SquarePanel) comp : null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        SquarePanel sp = panelAt(e.getX(), e.getY());
        if (sp == null) return;
        Piece p = sp.getOccupyingPiece();
        if (p == null) return;
        if ((model.isWhiteTurn() && p.getColor() == 1) || (!model.isWhiteTurn() && p.getColor() == 0)) {
            draggingPiece = p;
            sourceSquare = sp.getSquareData();
            sourcePanel = sp;
            ghostActive = true;
            dragging = true;
            dragX = e.getX();
            dragY = e.getY();
            sourcePanel.setDisplayPiece(false);
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging) return;
        dragX = e.getX();
        dragY = e.getY();
        repaint();
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!dragging) return;
        SquarePanel tp = panelAt(e.getX(), e.getY());
        boolean moved = false;

        if (tp != null) {
            Square targetSq = tp.getSquareData();

            if (targetSq.equals(sourceSquare)) {
                sourcePanel.setDisplayPiece(true);
                moved = true;
            } else {
                List<Square> legal = draggingPiece.getLegalMoves(model);
                if (legal.contains(targetSq) && model.isKingSafeAfterMove(draggingPiece, targetSq)) {
                    model.commitMove(sourceSquare, targetSq, draggingPiece);
                    tp.setDisplayPiece(true);

                    if (draggingPiece instanceof chess.model.pieces.Pawn) {
                        int row = targetSq.getYNum();
                        int color = draggingPiece.getColor();
                        if ((color == 0 && row == 7) || (color == 1 && row == 0)) {
                            promotePawn(targetSq, color);
                        }
                    }

                    timerStarter.startTimerIfNotStarted();

                    if (!model.hasAnyLegalMovesForCurrentPlayer()) {
                        if (model.isWhiteTurn()) {
                            if (model.isWhiteCheckmated()) {
                                timerStarter.gameOver(0);
                                return;
                            } else {
                                JOptionPane.showMessageDialog(this, "Stalemate!");
                                System.exit(0);
                            }
                        } else {
                            if (model.isBlackCheckmated()) {
                                timerStarter.gameOver(1);
                                return;
                            } else {
                                JOptionPane.showMessageDialog(this, "Stalemate!");
                                System.exit(0);
                            }
                        }
                    }

                    moved = true;
                }
            }
        }

        if (!moved) {
            sourceSquare.put(draggingPiece);
            sourcePanel.setDisplayPiece(true);
            shakeWindow();
        }

        draggingPiece = null;
        sourceSquare  = null;
        sourcePanel   = null;
        dragging      = false;
        ghostActive   = false;
        repaint();
    }

    private void promotePawn(Square square, int color) {
        String[] choices = { "Queen", "Rook", "Bishop", "Knight" };
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Promote pawn to:",
                "Promotion",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (choice == null) return;

        Piece promoted = switch (choice) {
            case "Rook" -> new chess.model.pieces.Rook(color, square, color == 1 ? "/images/wrook.png" : "/images/brook.png");
            case "Bishop" -> new chess.model.pieces.Bishop(color, square, color == 1 ? "/images/wbishop.png" : "/images/bbishop.png");
            case "Knight" -> new chess.model.pieces.Knight(color, square, color == 1 ? "/images/wknight.png" : "/images/bknight.png");
            default -> new chess.model.pieces.Queen(color, square, color == 1 ? "/images/wqueen.png" : "/images/bqueen.png");
        };

        square.put(promoted);
        square.put(promoted);

        if (color == 1) {
            model.getWhitePieces().removeIf(p -> p.getPosition() == square);
            model.getWhitePieces().add(promoted);
        } else {
            model.getBlackPieces().removeIf(p -> p.getPosition() == square);
            model.getBlackPieces().add(promoted);
        }

        repaint();
    }


    void shakeWindow() {
        Window w = SwingUtilities.getWindowAncestor(this);
        Point orig = w.getLocation();
        int dist = 10, times = 6;
        Timer shake = new Timer(50, null);
        shake.addActionListener(new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                int offset = (count % 2 == 0) ? dist : -dist;
                w.setLocation(orig.x + offset, orig.y);
                count++;
                if (count >= times) {
                    shake.stop();
                    w.setLocation(orig);
                }
            }
        });
        shake.start();
    }

    public void applyMove(ChessMove move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        Square fromSquare = model.getSquareArray()[fromRow][fromCol];
        Square toSquare = model.getSquareArray()[toRow][toCol];

        Piece piece = fromSquare.getOccupyingPiece();
        if (piece != null) {
            model.commitMove(fromSquare, toSquare, piece);
            revalidate();
            repaint();
        }
    }

    public void resetBoard() {
        removeAll(); // Clear all squares from the panel
        BoardState fresh = new BoardState(); // Create fresh board model
        Square[][] newSquares = fresh.getSquareArray();

        // Update internal model
        for (int row = 0; row < BoardState.SIZE; row++) {
            for (int col = 0; col < BoardState.SIZE; col++) {
                this.model.getSquareArray()[row][col] = newSquares[row][col];
            }
        }

        // Re-add new SquarePanels
        setLayout(new GridLayout(BoardState.SIZE, BoardState.SIZE));
        for (int r = 0; r < BoardState.SIZE; r++) {
            for (int c = 0; c < BoardState.SIZE; c++) {
                add(new SquarePanel(newSquares[r][c]));
            }
        }

        revalidate();
        repaint();
    }

    public boolean isWhiteTurn() {
        return model.isWhiteTurn();
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    @Override public void mouseMoved(MouseEvent e) { }
}