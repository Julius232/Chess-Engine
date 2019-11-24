package julius.game.chessengine.board;

import lombok.Data;

import java.util.List;

@Data
public class Position {

    private char xAchse;
    private int yAchse;

    public Position(char xAchse, int yAchse) {
        this.xAchse = xAchse;
        this.yAchse = yAchse;
    }

    public boolean  isPositionInFields(List<Field> fields) {
        return fields.stream()
                .anyMatch(field -> this.equals(field.getPosition()));
    }
}
