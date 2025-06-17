package chess.model.pieces;

import chess.model.BoardState;
import chess.model.Square;

import java.util.LinkedList;
import java.util.List;

public class Knight extends Piece {
    public Knight(int color, Square initSq, String imgFile) {
        super(color, initSq, imgFile);
    }

    @Override
    public List<Square> getLegalMoves(BoardState boardState) {
        LinkedList<Square> legalMoves = new LinkedList<>();
        Square[][] board = boardState.getSquareArray();
        int x = getPosition().getXNum();
        int y = getPosition().getYNum();
        int[][] moves = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] move : moves) {
            int newX = x + move[0];
            int newY = y + move[1];
            if (newX >= 0 && newX < board[0].length &&
                    newY >= 0 && newY < board.length) {
                Square sq = board[newY][newX];
                if (!sq.isOccupied() || sq.getOccupyingPiece().getColor() != getColor()) {
                    legalMoves.add(sq);
                }
            }
        }
        return legalMoves;
    }
}
