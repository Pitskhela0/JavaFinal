package chess.model.pieces;

import chess.model.BoardState;
import chess.model.Square;

import java.util.LinkedList;
import java.util.List;

public class King extends Piece {
    public King(int color, Square initSq, String imgFile) {
        super(color, initSq, imgFile);
    }

    @Override
    public List<Square> getLegalMoves(BoardState boardState) {
        LinkedList<Square> legalMoves = new LinkedList<>();
        Square[][] board = boardState.getSquareArray();
        int x = getPosition().getXNum();
        int y = getPosition().getYNum();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int newX = x + dx;
                int newY = y + dy;
                if (newX >= 0 && newX < board[0].length &&
                        newY >= 0 && newY < board.length) {
                    Square sq = board[newY][newX];
                    if (!sq.isOccupied() || sq.getOccupyingPiece().getColor() != getColor()) {
                        legalMoves.add(sq);
                    }
                }
            }
        }
        return legalMoves;
    }
}
