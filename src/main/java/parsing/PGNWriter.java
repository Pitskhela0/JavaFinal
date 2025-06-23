package parsing;

import shared.ChessMove;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PGNWriter {

    public static String generatePGN(List<ChessMove> moves, String whitePlayer, String blackPlayer, String result) {
        StringBuilder sb = new StringBuilder();

        // Header tags
        sb.append("[Event \"Casual Game\"]\n");
        sb.append("[Site \"Local\"]\n");
        sb.append("[Date \"").append(new SimpleDateFormat("yyyy.MM.dd").format(new Date())).append("\"]\n");
        sb.append("[White \"").append(whitePlayer).append("\"]\n");
        sb.append("[Black \"").append(blackPlayer).append("\"]\n");
        sb.append("[Result \"").append(result).append("\"]\n");
        sb.append("\n");

        // Moves
        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                sb.append((i / 2 + 1)).append(". ");
            }
            sb.append(toAlgebraic(moves.get(i))).append(" ");
        }

        sb.append(result);
        return sb.toString();
    }

    // Basic algebraic notation; refine this if you want proper Nf3, O-O, etc.
    private static String toAlgebraic(ChessMove move) {
        return String.valueOf((char) ('a' + move.getFromCol())) +
                (8 - move.getFromRow()) +
                String.valueOf((char) ('a' + move.getToCol())) +
                (8 - move.getToRow());
    }

}
