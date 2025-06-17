package chess.model.pieces;

import chess.model.BoardState;
import chess.model.Square;

import java.util.LinkedList;
import java.util.List;


public class Rook extends Piece {
    public Rook(int color, Square initSq, String imgFile) {
        super(color, initSq, imgFile);
    }

    @Override
    public List<Square> getLegalMoves(BoardState boardState) {
        LinkedList<Square> legalMoves = new LinkedList<>();
        Square[][] board = boardState.getSquareArray();
        int x = getPosition().getXNum();
        int y = getPosition().getYNum();
        int[] occups = getLinearOccupations(board, x, y);

        for (int i = occups[0]; i <= occups[1]; i++) {
            if (i != y) {
                legalMoves.add(board[i][x]);
            }
        }
        for (int i = occups[2]; i <= occups[3]; i++) {
            if (i != x) {
                legalMoves.add(board[y][i]);
            }
        }
        return legalMoves;
    }
}
