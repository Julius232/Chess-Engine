package julius.game.chessengine;

import lombok.Data;


@Data
public class Field {

    private final String color;
    private final Position position;

    public Field(String color, Position position) {
        this.color = color;
        this.position = position;
    }

}
