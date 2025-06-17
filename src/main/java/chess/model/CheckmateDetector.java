package chess.model;

import chess.model.pieces.King;
import chess.model.pieces.Piece;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CheckmateDetector {
    private final BoardState boardState;
    private final LinkedList<Piece> whitePieces;
    private final LinkedList<Piece> blackPieces;
    private final King whiteKing, blackKing;
    private final HashMap<Square, List<Piece>> whiteMoves;
    private final HashMap<Square, List<Piece>> blackMoves;

    public CheckmateDetector(BoardState boardState,
                             LinkedList<Piece> whitePieces,
                             LinkedList<Piece> blackPieces,
                             King whiteKing,
                             King blackKing) {
        this.boardState  = boardState;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.whiteKing   = whiteKing;
        this.blackKing   = blackKing;

        whiteMoves = new HashMap<>();
        blackMoves = new HashMap<>();
        for (Square[] row : boardState.getSquareArray()) {
            for (Square sq : row) {
                whiteMoves.put(sq, new LinkedList<>());
                blackMoves.put(sq, new LinkedList<>());
            }
        }
        update();
    }

    public void update() {
        whiteMoves.values().forEach(List::clear);
        blackMoves.values().forEach(List::clear);

        for (Iterator<Piece> it = whitePieces.iterator(); it.hasNext(); ) {
            Piece p = it.next();
            if (!(p instanceof King) && p.getPosition() != null) {
                for (Square sq : p.getLegalMoves(boardState)) {
                    whiteMoves.get(sq).add(p);
                }
            }
        }
        for (Iterator<Piece> it = blackPieces.iterator(); it.hasNext(); ) {
            Piece p = it.next();
            if (!(p instanceof King) && p.getPosition() != null) {
                for (Square sq : p.getLegalMoves(boardState)) {
                    blackMoves.get(sq).add(p);
                }
            }
        }
    }

    public boolean blackInCheck() {
        update();
        Square k = blackKing.getPosition();
        return !whiteMoves.get(k).isEmpty();
    }

    public boolean whiteInCheck() {
        update();
        Square k = whiteKing.getPosition();
        return !blackMoves.get(k).isEmpty();
    }

    public boolean blackCheckMated() {
        if (!blackInCheck()) return false;
        boolean mate = !canEvade(whiteMoves, blackKing);
        List<Piece> threats = whiteMoves.get(blackKing.getPosition());
        if (canCapture(blackMoves, threats, blackKing)) mate = false;
        if (canBlock(threats, blackMoves, blackKing))   mate = false;
        return mate;
    }

    public boolean whiteCheckMated() {
        if (!whiteInCheck()) return false;
        boolean mate = !canEvade(blackMoves, whiteKing);
        List<Piece> threats = blackMoves.get(whiteKing.getPosition());
        if (canCapture(whiteMoves, threats, whiteKing)) mate = false;
        if (canBlock(threats, whiteMoves, whiteKing))   mate = false;
        return mate;
    }

    private boolean canEvade(Map<Square, List<Piece>> attackMap, King king) {
        for (Square sq : king.getLegalMoves(boardState)) {
            if (testMove(king, sq) && attackMap.get(sq).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean canCapture(Map<Square, List<Piece>> attackMap,
                               List<Piece> threats,
                               King king) {
        if (threats.size() != 1) return false;
        Square t = threats.get(0).getPosition();
        if (king.getLegalMoves(boardState).contains(t) && testMove(king, t)) {
            return true;
        }
        for (Piece p : new ConcurrentLinkedDeque<>(attackMap.get(t))) {
            if (testMove(p, t)) return true;
        }
        return false;
    }

    private boolean canBlock(List<Piece> threats,
                             Map<Square, List<Piece>> blockMap,
                             King king) {
        if (threats.size() != 1) return false;
        Square t = threats.get(0).getPosition();
        Square k = king.getPosition();
        Square[][] brd = boardState.getSquareArray();

        if (k.getXNum() == t.getXNum()) {
            int minY = Math.min(k.getYNum(), t.getYNum());
            int maxY = Math.max(k.getYNum(), t.getYNum());
            for (int y = minY + 1; y < maxY; y++) {
                for (Piece p : new ConcurrentLinkedDeque<>(blockMap.get(brd[y][k.getXNum()]))) {
                    if (testMove(p, brd[y][k.getXNum()])) return true;
                }
            }
        }
        if (k.getYNum() == t.getYNum()) {
            int minX = Math.min(k.getXNum(), t.getXNum());
            int maxX = Math.max(k.getXNum(), t.getXNum());
            for (int x = minX + 1; x < maxX; x++) {
                for (Piece p : new ConcurrentLinkedDeque<>(blockMap.get(brd[k.getYNum()][x]))) {
                    if (testMove(p, brd[k.getYNum()][x])) return true;
                }
            }
        }
        return false;
    }

    public boolean testMove(Piece p, Square sq) {
        Piece origOcc = sq.getOccupyingPiece();
        Square orig    = p.getPosition();

        p.move(sq);
        update();
        boolean inCheck = (p.getColor() == 0 ? blackInCheck() : whiteInCheck());

        p.move(orig);
        sq.setOccupyingPiece(origOcc);
        update();

        return !inCheck;
    }
}
