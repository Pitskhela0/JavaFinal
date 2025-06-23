package shared;

import java.io.Serializable;

public class ChessMove implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final String specialMove; // for castling, en passant, promotion
    private final long timestamp;
    private final String moveType; // "normal", "resign", "error"

    // Normal move constructor
    public ChessMove(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, null, "normal");
    }

    // Move with special action constructor
    public ChessMove(int fromRow, int fromCol, int toRow, int toCol, String specialMove) {
        this(fromRow, fromCol, toRow, toCol, specialMove, "normal");
    }

    // Full constructor
    public ChessMove(int fromRow, int fromCol, int toRow, int toCol, String specialMove, String moveType) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.specialMove = specialMove;
        this.moveType = moveType;
        this.timestamp = System.currentTimeMillis();
    }

    // Special move constructors
    public static ChessMove resign() {
        return new ChessMove(-1, -1, -1, -1, null, "resign");
    }

    public static ChessMove error() {
        return new ChessMove(-1, -1, -1, -1, null, "error");
    }

    public static ChessMove fromString(String moveStr) {
        if (moveStr == null || moveStr.trim().isEmpty()) {
            return error();
        }

        moveStr = moveStr.trim().toLowerCase();

        if (moveStr.equals("resign")) {
            return resign();
        }

        try {
            // Parse formats like "e2e4", "e2-e4", or "0,1,2,3"
            if (moveStr.contains(",")) {
                // Format: "fromRow,fromCol,toRow,toCol"
                String[] parts = moveStr.split(",");
                if (parts.length >= 4) {
                    int fromRow = Integer.parseInt(parts[0]);
                    int fromCol = Integer.parseInt(parts[1]);
                    int toRow = Integer.parseInt(parts[2]);
                    int toCol = Integer.parseInt(parts[3]);
                    return new ChessMove(fromRow, fromCol, toRow, toCol);
                }
            } else if (moveStr.length() >= 4) {
                // Format: "e2e4" or "e2-e4"
                moveStr = moveStr.replace("-", "");
                if (moveStr.length() >= 4) {
                    int fromCol = moveStr.charAt(0) - 'a';
                    int fromRow = 8 - (moveStr.charAt(1) - '0');
                    int toCol = moveStr.charAt(2) - 'a';
                    int toRow = 8 - (moveStr.charAt(3) - '0');

                    if (isValidPosition(fromRow, fromCol) && isValidPosition(toRow, toCol)) {
                        return new ChessMove(fromRow, fromCol, toRow, toCol);
                    }
                }
            }
        } catch (Exception e) {
            // Invalid format
        }

        return error();
    }

    private static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // Getters
    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public String getSpecialMove() { return specialMove; }
    public String getMoveType() { return moveType; }
    public long getTimestamp() { return timestamp; }

    public boolean isNormalMove() { return "normal".equals(moveType); }
    public boolean isResign() { return "resign".equals(moveType); }
    public boolean isError() { return "error".equals(moveType); }

    @Override
    public String toString() {
        if (isResign()) return "resign";
        if (isError()) return "error";

        return String.format("Move: (%d,%d) -> (%d,%d)%s",
                fromRow, fromCol, toRow, toCol,
                specialMove != null ? " [" + specialMove + "]" : "");
    }

    // Convert to chess notation (e.g., "e2e4")
    public String toChessNotation() {
        if (!isNormalMove()) return moveType;

        char fromFile = (char)('a' + fromCol);
        char toFile = (char)('a' + toCol);
        int fromRank = 8 - fromRow;
        int toRank = 8 - toRow;
        return "" + fromFile + fromRank + toFile + toRank;
    }

    // Convert to coordinate format for easy parsing
    public String toCoordinateString() {
        if (!isNormalMove()) return moveType;
        return fromRow + "," + fromCol + "," + toRow + "," + toCol;
    }

    public String getFromSquare() {
        char file = (char) ('a' + fromCol); // 'a' to 'h'
        int rank = 8 - fromRow;             // '1' to '8'
        return "" + file + rank;
    }

    public String getToSquare() {
        char file = (char) ('a' + toCol);
        int rank = 8 - toRow;
        return "" + file + rank;
    }

}