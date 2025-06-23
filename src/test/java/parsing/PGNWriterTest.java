package parsing;

import org.junit.jupiter.api.Test;
import shared.ChessMove;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PGNWriterTest {
    @Test
    void testGeneratePGN_emptyMoveList() {
        List<ChessMove> moves = List.of();
        String pgn = PGNWriter.generatePGN(moves, "W", "B", "1/2-1/2");
        assertTrue(pgn.contains("1/2-1/2"));
        assertFalse(pgn.contains("1."));
    }

    @Test
    void testGeneratePGN_basicMoves() {
        List<ChessMove> moves = Arrays.asList(
                new ChessMove(6, 4, 4, 4) { @Override public String toChessNotation() { return "e2e4"; }},
                new ChessMove(1, 4, 3, 4) { @Override public String toChessNotation() { return "e7e5"; }},
                new ChessMove(7, 6, 5, 5) { @Override public String toChessNotation() { return "g1f3"; }},
                new ChessMove(0, 1, 2, 2) { @Override public String toChessNotation() { return "b8c6"; }}
        );

        String white = "Alice";
        String black = "Bob";
        String result = "1-0";

        String pgn = PGNWriter.generatePGN(moves, white, black, result);

        System.out.println("Generated PGN:\n" + pgn);

        assertNotNull(pgn);
        assertTrue(pgn.contains("[White \"Alice\"]"));
        assertTrue(pgn.contains("[Black \"Bob\"]"));
        assertTrue(pgn.contains("1. e2e4 e7e5"));
        assertTrue(pgn.contains("2. g1f3 b8c6"));
        assertTrue(pgn.endsWith("1-0"));
    }
}
