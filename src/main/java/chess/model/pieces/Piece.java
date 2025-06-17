package chess.model.pieces;

import chess.model.BoardState;
import chess.model.Square;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

public abstract class Piece {
    private final int color;
    private Square currentSquare;
    private BufferedImage img;

    public Piece(int color, Square initSq, String imgFile) {
        this.color = color;
        this.currentSquare = initSq;
        URL imgUrl = getClass().getResource(imgFile);
        if (imgUrl == null) {
            System.err.println("Image resource not found: " + imgFile);
        }
        try {
            img = ImageIO.read(Objects.requireNonNull(imgUrl, "Image resource is null: " + imgFile));
        } catch (IOException e) {
            System.err.println("Failed to load image: " + imgFile + " : " + e.getMessage());
        }
    }

    public boolean move(Square fin) {
        Piece occupant = fin.getOccupyingPiece();
        if (occupant != null && occupant.getColor() == this.color) {
            return false;
        }
        if (currentSquare != null) {
            currentSquare.setOccupyingPiece(null);
        }
        this.currentSquare = fin;
        fin.setOccupyingPiece(this);
        return true;
    }

    public Square getPosition() {
        return currentSquare;
    }

    public void setPosition(Square sq) {
        this.currentSquare = sq;
    }

    public int getColor() {
        return color;
    }

    public Image getImage() {
        return img;
    }

    public abstract List<Square> getLegalMoves(BoardState boardState);

    protected int[] getLinearOccupations(Square[][] board, int x, int y) {
        int lastYAbove = 0;
        int lastYBelow = board.length - 1;
        int lastXLeft = 0;
        int lastXRight = board[0].length - 1;

        for (int i = 0; i < y; i++) {
            if (board[i][x].getOccupyingPiece() != null) {
                if (board[i][x].getOccupyingPiece().getColor() != this.color) {
                    lastYAbove = i;
                } else {
                    lastYAbove = i + 1;
                }
            }
        }
        for (int i = board.length - 1; i > y; i--) {
            if (board[i][x].getOccupyingPiece() != null) {
                if (board[i][x].getOccupyingPiece().getColor() != this.color) {
                    lastYBelow = i;
                } else {
                    lastYBelow = i - 1;
                }
            }
        }
        for (int i = 0; i < x; i++) {
            if (board[y][i].getOccupyingPiece() != null) {
                if (board[y][i].getOccupyingPiece().getColor() != this.color) {
                    lastXLeft = i;
                } else {
                    lastXLeft = i + 1;
                }
            }
        }
        for (int i = board[y].length - 1; i > x; i--) {
            if (board[y][i].getOccupyingPiece() != null) {
                if (board[y][i].getOccupyingPiece().getColor() != this.color) {
                    lastXRight = i;
                } else {
                    lastXRight = i - 1;
                }
            }
        }
        return new int[]{lastYAbove, lastYBelow, lastXLeft, lastXRight};
    }

    protected List<Square> getDiagonalOccupations(Square[][] board, int x, int y) {
        LinkedList<Square> diag = new LinkedList<>();
        int xNW = x - 1, yNW = y - 1;
        while (xNW >= 0 && yNW >= 0) {
            Square sq = board[yNW][xNW];
            if (sq.getOccupyingPiece() != null) {
                if (sq.getOccupyingPiece().getColor() != this.color) {
                    diag.add(sq);
                }
                break;
            } else {
                diag.add(sq);
            }
            xNW--;
            yNW--;
        }
        int xSW = x - 1, ySW = y + 1;
        while (xSW >= 0 && ySW < board.length) {
            Square sq = board[ySW][xSW];
            if (sq.getOccupyingPiece() != null) {
                if (sq.getOccupyingPiece().getColor() != this.color) {
                    diag.add(sq);
                }
                break;
            } else {
                diag.add(sq);
            }
            xSW--;
            ySW++;
        }
        int xSE = x + 1, ySE = y + 1;
        while (xSE < board[0].length && ySE < board.length) {
            Square sq = board[ySE][xSE];
            if (sq.getOccupyingPiece() != null) {
                if (sq.getOccupyingPiece().getColor() != this.color) {
                    diag.add(sq);
                }
                break;
            } else {
                diag.add(sq);
            }
            xSE++;
            ySE++;
        }
        int xNE = x + 1, yNE = y - 1;
        while (xNE < board[0].length && yNE >= 0) {
            Square sq = board[yNE][xNE];
            if (sq.getOccupyingPiece() != null) {
                if (sq.getOccupyingPiece().getColor() != this.color) {
                    diag.add(sq);
                }
                break;
            } else {
                diag.add(sq);
            }
            xNE++;
            yNE--;
        }
        return diag;
    }
}
