package julius.game.chessengine.board;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Data
@Log4j2
public class Position {

    private char x;
    private int y;

    public Position(char x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position other) {
        this.x = other.x;
        this.y = other.y;
    }

    public String toString() {
        return x + String.valueOf(y);
    }

    public static int convertStringToIndex(String positionStr) {
        if (positionStr.length() != 2) {
            throw new IllegalArgumentException("Invalid position string: " + positionStr);
        }
        char fileChar = positionStr.charAt(0);
        int rank = Character.getNumericValue(positionStr.charAt(1));

        if (fileChar < 'a' || fileChar > 'h' || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position out of bounds: " + positionStr);
        }

        int file = fileChar - 'a'; // Convert file to 0-7 range
        rank = rank - 1; // Convert rank to 0-based index

        return rank * 8 + file;
    }
}
