package julius.game.chessengine.figures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import lombok.Data;

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
    abstract public Board move(Board board, Field toField);

    abstract public Board attack(Board board, Field toField);

    abstract public List<Field> getPossibleFields(Board board);

    @JsonIgnore
    public Position getCurrentPosition() {
        return this.currentField.getPosition();
    }

    @JsonIgnore
    public char getPosX() {
        return getCurrentPosition().getXAchse();
    }

    @JsonIgnore
    public int getPosY() {
        return getCurrentPosition().getYAchse();
    }

}
