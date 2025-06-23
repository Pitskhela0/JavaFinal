package simulation;

import java.util.Objects;

public class Position {
    private final char x; // File (a-h)
    private final int y;  // Rank (1-8)

    public Position(char x, int y) {
        this.x = x;
        this.y = y;
    }

    // Convert to 0-based array index for file (a=0, b=1, etc.)
    public int getX() {
        return y - 1;
    }

    // Convert to 0-based array index for rank (1=0, 2=1, etc.)
    public int getY() {
        return x - 'a';
    }

    // Get the original file character (a-h)
    public char getFile() {
        return x;
    }

    // Get the original rank number (1-8)
    public int getRank() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "" + x + y;
    }
}