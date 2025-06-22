package shared;

import chess.model.BoardState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testDefaultGameState() {
        GameState gameState = new GameState();

        assertTrue(gameState.isWhiteTurn());
        assertFalse(gameState.isGameOver());
        assertFalse(gameState.isWhiteInCheck());
        assertFalse(gameState.isBlackInCheck());
        assertNull(gameState.getWinner());
        assertEquals(0, gameState.getMoveCount());
        assertNotNull(gameState.getBoard());
        assertEquals(8, gameState.getBoard().length);
        assertEquals(8, gameState.getBoard()[0].length);
    }

    @Test
    public void testGameStateFromBoardState() {
        BoardState boardState = new BoardState();
        GameState gameState = new GameState(boardState);

        assertTrue(gameState.isWhiteTurn());
        assertFalse(gameState.isGameOver());

        // Check that pieces are properly converted
        String[][] board = gameState.getBoard();
        assertNotNull(board[0][0]); // Should have rook
        assertNotNull(board[1][0]); // Should have pawn
        assertTrue(board[0][0].startsWith("b")); // Black piece
        assertTrue(board[7][0].startsWith("w")); // White piece
    }

    @Test
    public void testIncrementMoveCount() {
        GameState gameState = new GameState();
        assertEquals(0, gameState.getMoveCount());

        gameState.incrementMoveCount();
        assertEquals(1, gameState.getMoveCount());

        gameState.incrementMoveCount();
        assertEquals(2, gameState.getMoveCount());
    }

    @Test
    public void testSetters() {
        GameState gameState = new GameState();

        gameState.setWhiteTurn(false);
        assertFalse(gameState.isWhiteTurn());

        gameState.setGameOver(true);
        assertTrue(gameState.isGameOver());

        gameState.setWinner("White");
        assertEquals("White", gameState.getWinner());

        gameState.setWhiteInCheck(true);
        assertTrue(gameState.isWhiteInCheck());

        gameState.setLastMove("e2e4");
        assertEquals("e2e4", gameState.getLastMove());
    }
}