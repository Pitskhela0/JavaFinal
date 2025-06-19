package serverSide;

import chess.model.BoardState;
import shared.GameState;

/**
 * Simplified Board class that acts as a wrapper around the chess BoardState
 * for server-side game state management. This keeps the server's board state
 * separate from the client UI components.
 */
public class Board {
    private BoardState boardState;
    private GameState lastGameState;

    public Board() {
        this.boardState = new BoardState();
        this.lastGameState = new GameState(boardState);
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public GameState getGameState() {
        // Always return a fresh GameState to ensure consistency
        GameState currentState = new GameState(boardState);
        this.lastGameState = currentState;
        return currentState;
    }

    public GameState getLastGameState() {
        return lastGameState;
    }

    /**
     * Reset the board to initial state
     */
    public void reset() {
        this.boardState = new BoardState();
        this.lastGameState = new GameState(boardState);
    }

    /**
     * Check if the game is over (checkmate or stalemate)
     */
    public boolean isGameOver() {
        return !boardState.hasAnyLegalMovesForCurrentPlayer();
    }

    /**
     * Get the winner of the game, if any
     */
    public String getWinner() {
        if (!isGameOver()) {
            return null;
        }

        if (boardState.isWhiteCheckmated()) {
            return "Black";
        } else if (boardState.isBlackCheckmated()) {
            return "White";
        } else {
            return "Draw"; // Stalemate
        }
    }
}