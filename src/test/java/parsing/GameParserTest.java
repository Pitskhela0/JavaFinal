package parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulation.Color;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameParserTest {

    private GameParser parser;

    @BeforeEach
    public void setup() {
        parser = new GameParser();
    }

    @Test
    public void testParsingMoves_singleGame() throws IOException {
        String samplePGN = """
            [Event "Test Match"]
            [Site "Testville"]
            [Date "2025.06.22"]
            [Round "1"]
            [White "Alice"]
            [Black "Bob"]
            [Result "1-0"]

            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 1-0
            """;

        Path tempFile = Files.createTempFile("sample", ".pgn");
        Files.writeString(tempFile, samplePGN);

        List<Record> records = parser.parsingMoves(tempFile.toString());

        assertEquals(1, records.size());

        Record record = records.get(0);
        assertNotNull(record);
        assertEquals("Alice", record.getTags().get("White"));
        assertEquals("Bob", record.getTags().get("Black"));
        assertEquals("1-0", record.getResult());

        Map<Integer, Move[]> moves = record.getRecord();
        assertEquals(8, moves.size()); // 8 full moves

        Move[] round1 = moves.get(1);
        assertNotNull(round1);
        assertEquals("e4", round1[0].getAction());
        assertEquals("e5", round1[1].getAction());
        assertEquals(Color.white, round1[0].getColor());
        assertEquals(Color.black, round1[1].getColor());

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testParsingMoves_invalidFile() {
        List<Record> records = parser.parsingMoves("non_existent_file.pgn");
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }
}
