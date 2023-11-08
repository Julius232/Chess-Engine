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


    public boolean isPositionInFields(List<Field> fields) {
        return fields.stream()
                .anyMatch(field -> this.equals(field.getPosition()));
    }

    public String toString() {
        return x + String.valueOf(y);
    }

    public static Position convertStringToPosition(String positionStr) {
        if (positionStr.length() != 2) {
            throw new IllegalArgumentException("Invalid position string: " + positionStr);
        }
        char file = positionStr.charAt(0);
        int rank = Character.getNumericValue(positionStr.charAt(1));

        if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position out of bounds: " + positionStr);
        }

        return new Position(file, rank);
    }
}
