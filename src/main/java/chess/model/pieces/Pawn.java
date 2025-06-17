package chess.model.pieces;


import chess.model.BoardState;
import chess.model.Square;

import java.util.LinkedList;
import java.util.List;

public class Pawn extends Piece {
    private boolean wasMoved;

    public Pawn(int color, Square initSq, String imgFile) {
        super(color, initSq, imgFile);
        wasMoved = false;
    }

    @Override
    public boolean move(Square fin) {
        boolean moved = super.move(fin);
        if (moved && !wasMoved) {
            wasMoved = true;
        }
        return moved;
    }

    @Override
    public List<Square> getLegalMoves(BoardState boardState) {
        LinkedList<Square> legalMoves = new LinkedList<>();
        Square[][] board = boardState.getSquareArray();
        int x = getPosition().getXNum();
        int y = getPosition().getYNum();
        int c = getColor();

        if (c == 0) {
            if (y + 1 < board.length && !board[y + 1][x].isOccupied()) {
                legalMoves.add(board[y + 1][x]);
                if (!wasMoved && y + 2 < board.length && !board[y + 2][x].isOccupied()) {
                    legalMoves.add(board[y + 2][x]);
                }
            }
            if (x + 1 < board[0].length && y + 1 < board.length &&
                    board[y + 1][x + 1].isOccupied() &&
                    board[y + 1][x + 1].getOccupyingPiece().getColor() != c) {
                legalMoves.add(board[y + 1][x + 1]);
            }
            if (x - 1 >= 0 && y + 1 < board.length &&
                    board[y + 1][x - 1].isOccupied() &&
                    board[y + 1][x - 1].getOccupyingPiece().getColor() != c) {
                legalMoves.add(board[y + 1][x - 1]);
            }
        } else {
            if (y - 1 >= 0 && !board[y - 1][x].isOccupied()) {
                legalMoves.add(board[y - 1][x]);
                if (!wasMoved && y - 2 >= 0 && !board[y - 2][x].isOccupied()) {
                    legalMoves.add(board[y - 2][x]);
                }
            }
            if (x + 1 < board[0].length && y - 1 >= 0 &&
                    board[y - 1][x + 1].isOccupied() &&
                    board[y - 1][x + 1].getOccupyingPiece().getColor() != c) {
                legalMoves.add(board[y - 1][x + 1]);
            }
            if (x - 1 >= 0 && y - 1 >= 0 &&
                    board[y - 1][x - 1].isOccupied() &&
                    board[y - 1][x - 1].getOccupyingPiece().getColor() != c) {
                legalMoves.add(board[y - 1][x - 1]);
            }
        }

        return legalMoves;
    }

    public void setWasMoved(boolean moved) {
        this.wasMoved = moved;
    }

    public boolean hasMoved() {
        return wasMoved;
    }
}
