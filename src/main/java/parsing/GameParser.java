package parsing;

import simulation.Color;
import simulation.Position;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameParser {
    private static final Pattern TAG_PATTERN = Pattern.compile("^\\[([A-Za-z]+)\\s+\"(.*)\"\\]$");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\d+\\.\\s*([a-h][1-8][a-h][1-8])(\\s+([a-h][1-8][a-h][1-8]))?");
    private static final Pattern RESULT_PATTERN = Pattern.compile("(1-0|0-1|1/2-1/2|\\*)");

    public static Record parsePGN(String pgn) {
        Map<String, String> tags = new HashMap<>();
        String[] lines = pgn.split("\n");

        StringBuilder moveText = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("[")) {
                Matcher tagMatch = TAG_PATTERN.matcher(line.trim());
                if (tagMatch.matches()) {
                    tags.put(tagMatch.group(1), tagMatch.group(2));
                }
            } else {
                moveText.append(line).append(" ");
            }
        }

        String movesStr = moveText.toString().trim();
        String result = null;
        Matcher resultMatcher = RESULT_PATTERN.matcher(movesStr);
        if (resultMatcher.find()) {
            result = resultMatcher.group(1);
            movesStr = movesStr.replace(result, "").trim();
        }

        Map<Integer, Move[]> moves = new HashMap<>();
        Matcher moveMatcher = LINE_PATTERN.matcher(movesStr);
        int round = 1;
        while (moveMatcher.find()) {
            String whiteMoveStr = moveMatcher.group(1);
            String blackMoveStr = moveMatcher.group(3); // may be null

            Move whiteMove = toMove(whiteMoveStr, Color.white);
            Move blackMove = blackMoveStr != null ? toMove(blackMoveStr, Color.black) : null;

            moves.put(round, new Move[]{whiteMove, blackMove});
            round++;
        }

        return new Record(tags, moves, result != null ? result : "*");
    }

    private static Move toMove(String uci, Color color) {
        if (uci == null || uci.length() < 4) return null;
        char fromFile = uci.charAt(0);
        int fromRank = Character.getNumericValue(uci.charAt(1));
        char toFile = uci.charAt(2);
        int toRank = Character.getNumericValue(uci.charAt(3));

        Position from = new Position(fromFile, fromRank);
        Position to = new Position(toFile, toRank);

        String action = "" + fromFile + fromRank + toFile + toRank;
        return new Move(action, null, "uci", color);
    }
}
