package shared;

import chess.model.BoardState;
import chess.model.Square;
import chess.model.pieces.*;
import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String[][] board; // 8x8 array representing piece positions
    private boolean whiteTurn;
    private boolean whiteInCheck;
    private boolean blackInCheck;
    private boolean gameOver;
    private String winner;
    private int moveCount;
    private String lastMove;

    public GameState() {
        this.board = new String[8][8];
        this.whiteTurn = true;
        this.whiteInCheck = false;
        this.blackInCheck = false;
        this.gameOver = false;
        this.winner = null;
        this.moveCount = 0;
        this.lastMove = "";
    }

    // Constructor from BoardState
    public GameState(BoardState boardState) {
        this();
        updateFromBoardState(boardState);
    }

    private void updateFromBoardState(BoardState boardState) {
        Square[][] squares = boardState.getSquareArray();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col].getOccupyingPiece();
                if (piece != null) {
                    board[row][col] = getPieceNotation(piece);
                } else {
                    board[row][col] = null;
                }
            }
        }

        this.whiteTurn = boardState.isWhiteTurn();

        // Check for checkmate/stalemate
        if (!boardState.hasAnyLegalMovesForCurrentPlayer()) {
            this.gameOver = true;
            if (boardState.isWhiteCheckmated()) {
                this.winner = "Black";
            } else if (boardState.isBlackCheckmated()) {
                this.winner = "White";
            } else {
                this.winner = "Draw"; // Stalemate
            }
        }
    }

    private String getPieceNotation(Piece piece) {
        String color = piece.getColor() == 1 ? "w" : "b";
        String type;

        if (piece instanceof Pawn) type = "pawn";
        else if (piece instanceof Rook) type = "rook";
        else if (piece instanceof Knight) type = "knight";
        else if (piece instanceof Bishop) type = "bishop";
        else if (piece instanceof Queen) type = "queen";
        else if (piece instanceof King) type = "king";
        else type = "unknown";

        return color + type;
    }

    // Convert piece notation back to image path
    public String getPieceImagePath(String pieceNotation) {
        if (pieceNotation == null) return null;

        String imagePath = "/images/";

        if (pieceNotation.startsWith("w")) {
            imagePath += "w";
        } else {
            imagePath += "b";
        }

        if (pieceNotation.contains("pawn")) imagePath += "pawn";
        else if (pieceNotation.contains("rook")) imagePath += "rook";
        else if (pieceNotation.contains("knight")) imagePath += "knight";
        else if (pieceNotation.contains("bishop")) imagePath += "bishop";
        else if (pieceNotation.contains("queen")) imagePath += "queen";
        else if (pieceNotation.contains("king")) imagePath += "king";

        imagePath += ".png";
        return imagePath;
    }

    // Getters and setters
    public String[][] getBoard() { return board; }
    public boolean isWhiteTurn() { return whiteTurn; }
    public void setWhiteTurn(boolean whiteTurn) { this.whiteTurn = whiteTurn; }
    public boolean isWhiteInCheck() { return whiteInCheck; }
    public void setWhiteInCheck(boolean whiteInCheck) { this.whiteInCheck = whiteInCheck; }
    public boolean isBlackInCheck() { return blackInCheck; }
    public void setBlackInCheck(boolean blackInCheck) { this.blackInCheck = blackInCheck; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public int getMoveCount() { return moveCount; }
    public void setMoveCount(int moveCount) { this.moveCount = moveCount; }
    public String getLastMove() { return lastMove; }
    public void setLastMove(String lastMove) { this.lastMove = lastMove; }

    public void incrementMoveCount() { this.moveCount++; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameState{");
        sb.append("moveCount=").append(moveCount);
        sb.append(", whiteTurn=").append(whiteTurn);
        sb.append(", gameOver=").append(gameOver);
        sb.append(", winner='").append(winner).append('\'');
        sb.append(", lastMove='").append(lastMove).append('\'');
        sb.append('}');
        return sb.toString();
    }
}