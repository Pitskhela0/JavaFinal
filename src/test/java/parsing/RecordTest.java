package parsing;

import org.junit.jupiter.api.Test;
import simulation.Color;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {

    @Test
    void testGettersWorkCorrectly() {
        Map<String, String> tags = new HashMap<>();
        tags.put("Event", "Test Game");
        tags.put("White", "Alice");
        tags.put("Black", "Bob");

        Map<Integer, Move[]> moveMap = new LinkedHashMap<>();

        Move white1 = new Move("e4", "{good move}", "$1", Color.white);
        Move black1 = new Move("e5", null, null, Color.black);
        Move white2 = new Move("Nf3", null, "$2", Color.white);
        Move black2 = new Move("Nc6", null, null, Color.black);

        moveMap.put(1, new Move[]{white1, black1});
        moveMap.put(2, new Move[]{white2, black2});

        String result = "1-0";

        Record record = new Record(tags, moveMap, result);

        // Test getResult
        assertEquals("1-0", record.getResult());

        // Test getTags
        assertEquals("Test Game", record.getTags().get("Event"));
        assertEquals("Alice", record.getTags().get("White"));
        assertEquals("Bob", record.getTags().get("Black"));

        // Test getRecord map
        assertEquals(2, record.getRecord().size());
        assertArrayEquals(new Move[]{white1, black1}, record.getRecord().get(1));
        assertArrayEquals(new Move[]{white2, black2}, record.getRecord().get(2));

        // Test getMoves (flattened list)
        List<Move> allMoves = record.getMoves();
        assertEquals(4, allMoves.size());
        assertEquals("e4", allMoves.get(0).getAction());
        assertEquals("e5", allMoves.get(1).getAction());
        assertEquals("Nf3", allMoves.get(2).getAction());
        assertEquals("Nc6", allMoves.get(3).getAction());
    }

    @Test
    void testGetMovesHandlesNullsSafely() {
        Map<Integer, Move[]> moveMap = new HashMap<>();
        moveMap.put(1, new Move[]{new Move("e4", null, null, Color.white), null});

        Record record = new Record(Collections.emptyMap(), moveMap, "*");

        List<Move> moves = record.getMoves();
        assertEquals(2, moves.size());
        assertNotNull(moves.get(0));
        assertNull(moves.get(1));
    }
}
