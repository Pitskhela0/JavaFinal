package parsing;

import org.junit.jupiter.api.Test;
import simulation.Color;

import static org.junit.jupiter.api.Assertions.*;

public class MoveTest {

    @Test
    void testMoveProperties() {
        Move move = new Move("e4", "{Best opening}", "$1", Color.white);

        assertEquals("e4", move.getAction());
        assertEquals("{Best opening}", move.getComment());
        assertEquals("$1", move.getAnnotation());
        assertEquals(Color.white, move.getColor());
    }

    @Test
    void testMoveReturnsCorrectAction() {
        Move move = new Move("Nf3", null, null, Color.white);
        assertEquals("Nf3", move.getAction());
    }

    @Test
    void testNullFieldsHandledCorrectly() {
        Move move = new Move("d5", null, null, Color.black);

        assertEquals("d5", move.getAction());
        assertNull(move.getComment());
        assertNull(move.getAnnotation());
        assertEquals(Color.black, move.getColor());
    }
}
