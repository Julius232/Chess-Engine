package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public abstract class Figure {

    private String type;
    private String color;
    private Field currentField;

    //Constructor
    public Figure(String color, String type, Field currentField) {
        this.color = color;
        this.type = type;
        this.currentField = currentField;
    }

    //Methods
    abstract void move(Field toField);

    abstract void kill(Field toField);

    abstract List<Field> getPossibleFields(Board board);

    abstract boolean isLegalMove(Field toField);

    public Position getCurrentPosition() {
        return this.currentField.getPosition();
    }

    public char getPosX() {
        return getCurrentPosition().getXAchse();
    }

    public int getPosY() {
        return getCurrentPosition().getYAchse();
    }

}
