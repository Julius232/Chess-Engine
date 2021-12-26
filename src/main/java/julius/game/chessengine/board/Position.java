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

    public boolean isPositionInFields(List<Field> fields) {
        return fields.stream()
                .anyMatch(field -> this.equals(field.getPosition()));
    }

    public String toString() {
        return x + String.valueOf(y);
    }
}
