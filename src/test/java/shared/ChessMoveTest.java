package shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChessMoveTest {

    @Test
    public void testNormalMoveCreation() {
        ChessMove move = new ChessMove(1, 0, 3, 0);
        assertEquals(1, move.getFromRow());
        assertEquals(0, move.getFromCol());
        assertEquals(3, move.getToRow());
        assertEquals(0, move.getToCol());
        assertTrue(move.isNormalMove());
        assertFalse(move.isResign());
        assertFalse(move.isError());
    }

    @Test
    public void testResignMove() {
        ChessMove resignMove = ChessMove.resign();
        assertTrue(resignMove.isResign());
        assertFalse(resignMove.isNormalMove());
        assertEquals("resign", resignMove.getMoveType());
    }

    @Test
    public void testErrorMove() {
        ChessMove errorMove = ChessMove.error();
        assertTrue(errorMove.isError());
        assertFalse(errorMove.isNormalMove());
        assertEquals("error", errorMove.getMoveType());
    }

    @Test
    public void testFromStringValidMoves() {
        // Test coordinate format
        ChessMove move1 = ChessMove.fromString("1,0,3,0");
        assertTrue(move1.isNormalMove());
        assertEquals(1, move1.getFromRow());
        assertEquals(0, move1.getFromCol());

        // Test algebraic notation
        ChessMove move2 = ChessMove.fromString("e2e4");
        assertTrue(move2.isNormalMove());
        assertEquals(6, move2.getFromRow()); // e2 = row 6 in 0-indexed
        assertEquals(4, move2.getFromCol()); // e = column 4

        // Test resign
        ChessMove resignMove = ChessMove.fromString("resign");
        assertTrue(resignMove.isResign());
    }

    @Test
    public void testFromStringInvalidMoves() {
        ChessMove errorMove1 = ChessMove.fromString("");
        assertTrue(errorMove1.isError());

        ChessMove errorMove2 = ChessMove.fromString("invalid");
        assertTrue(errorMove2.isError());

        ChessMove errorMove3 = ChessMove.fromString(null);
        assertTrue(errorMove3.isError());
    }

    @Test
    public void testToChessNotation() {
        ChessMove move = new ChessMove(6, 4, 4, 4); // e2 to e4
        assertEquals("e2e4", move.toChessNotation());

        ChessMove resignMove = ChessMove.resign();
        assertEquals("resign", resignMove.toChessNotation());
    }
}