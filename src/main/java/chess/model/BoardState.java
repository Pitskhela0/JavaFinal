package chess.model;

import chess.model.pieces.*;
import java.util.LinkedList;
import java.util.List;

public class BoardState {
    public static final int SIZE = 8;

    private final Square[][] board = new Square[SIZE][SIZE];
    private boolean whiteTurn = true;
    private final List<Piece> whitePieces = new LinkedList<>();
    private final List<Piece> blackPieces = new LinkedList<>();
    private King whiteKing, blackKing;

    public BoardState() {
        initializeSquares();
        initializePieces();
    }

    private void initializeSquares() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int color = ((x + y) % 2 == 0) ? 1 : 0;
                board[y][x] = new Square(x, y, color);
            }
        }
    }

    private void initializePieces() {
        for (int x = 0; x < SIZE; x++) {
            Pawn bp = new Pawn(0, board[1][x], "/images/bpawn.png");
            board[1][x].setOccupyingPiece(bp);
            blackPieces.add(bp);

            Pawn wp = new Pawn(1, board[6][x], "/images/wpawn.png");
            board[6][x].setOccupyingPiece(wp);
            whitePieces.add(wp);
        }
        placeMajor(0, 0, 0, "/images/brook.png", Rook.class, blackPieces);
        placeMajor(0, 1, 0, "/images/bknight.png", Knight.class, blackPieces);
        placeMajor(0, 2, 0, "/images/bbishop.png", Bishop.class, blackPieces);
        Queen bq = new Queen(0, board[0][3], "/images/bqueen.png");
        board[0][3].setOccupyingPiece(bq); blackPieces.add(bq);
        blackKing = new King(0, board[0][4], "/images/bking.png");
        board[0][4].setOccupyingPiece(blackKing); blackPieces.add(blackKing);
        placeMajor(0, 5, 0, "/images/bbishop.png", Bishop.class, blackPieces);
        placeMajor(0, 6, 0, "/images/bknight.png", Knight.class, blackPieces);
        placeMajor(0, 7, 0, "/images/brook.png", Rook.class, blackPieces);

        placeMajor(7, 0, 1, "/images/wrook.png", Rook.class, whitePieces);
        placeMajor(7, 1, 1, "/images/wknight.png", Knight.class, whitePieces);
        placeMajor(7, 2, 1, "/images/wbishop.png", Bishop.class, whitePieces);
        Queen wq = new Queen(1, board[7][3], "/images/wqueen.png");
        board[7][3].setOccupyingPiece(wq); whitePieces.add(wq);
        whiteKing = new King(1, board[7][4], "/images/wking.png");
        board[7][4].setOccupyingPiece(whiteKing); whitePieces.add(whiteKing);
        placeMajor(7, 5, 1, "/images/wbishop.png", Bishop.class, whitePieces);
        placeMajor(7, 6, 1, "/images/wknight.png", Knight.class, whitePieces);
        placeMajor(7, 7, 1, "/images/wrook.png", Rook.class, whitePieces);
    }

    private <T extends Piece> void placeMajor(int row, int col, int color,
                                              String img, Class<T> cls,
                                              List<Piece> list) {
        try {
            T p = cls.getConstructor(int.class, Square.class, String.class)
                    .newInstance(color, board[row][col], img);
            board[row][col].setOccupyingPiece(p);
            list.add(p);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + cls, e);
        }
    }

    public Square[][] getSquareArray() {
        return board;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void toggleTurn() {
        whiteTurn = !whiteTurn;
    }

    public List<Piece> getWhitePieces() {
        return whitePieces;
    }

    public List<Piece> getBlackPieces() {
        return blackPieces;
    }

    public boolean isKingSafeAfterMove(Piece p, Square target) {
        Square original = p.getPosition();
        Piece captured = target.getOccupyingPiece();

        original.removePiece();
        target.setOccupyingPiece(p);
        p.setPosition(target);

        LinkedList<Piece> whiteSim = new LinkedList<>(whitePieces);
        LinkedList<Piece> blackSim = new LinkedList<>(blackPieces);

        if (p.getColor() == 0) {
            blackSim.remove(p);
        } else {
            whiteSim.remove(p);
        }

        if (captured != null) {
            if (captured.getColor() == 0) blackSim.remove(captured);
            else whiteSim.remove(captured);
        }

        CheckmateDetector detector = new CheckmateDetector(this, whiteSim, blackSim, whiteKing, blackKing);
        boolean isSafe = p.getColor() == 1 ? !detector.whiteInCheck() : !detector.blackInCheck();

        target.setOccupyingPiece(captured);
        original.setOccupyingPiece(p);
        p.setPosition(original);

        return isSafe;
    }

    public boolean isWhiteCheckmated() {
        CheckmateDetector dt =
                new CheckmateDetector(this,
                        new LinkedList<>(whitePieces),
                        new LinkedList<>(blackPieces),
                        whiteKing, blackKing);
        return dt.whiteCheckMated();
    }

    public boolean isBlackCheckmated() {
        CheckmateDetector dt =
                new CheckmateDetector(this,
                        new LinkedList<>(whitePieces),
                        new LinkedList<>(blackPieces),
                        whiteKing, blackKing);
        return dt.blackCheckMated();
    }

    public void commitMove(Square from, Square to, Piece mover) {
        Piece captured = to.getOccupyingPiece();
        if (captured != null) {
            if (captured.getColor() == 1)
                whitePieces.remove(captured);
            else
                blackPieces.remove(captured);
        }

        from.removePiece();
        to.put(mover);
        if (mover instanceof Pawn) {
            ((Pawn) mover).setWasMoved(true);
        }
        toggleTurn();
    }

    public boolean hasAnyLegalMovesForCurrentPlayer() {
        List<Piece> activePieces = whiteTurn ? whitePieces : blackPieces;
        for (Piece p : activePieces) {
            List<Square> legalMoves = p.getLegalMoves(this);
            for (Square move : legalMoves) {
                if (isKingSafeAfterMove(p, move)) return true;
            }
        }
        return false;
    }

}
