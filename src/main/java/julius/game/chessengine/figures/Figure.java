package julius.game.chessengine.figures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.MoveField;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public abstract class Figure {

    private String type;
    private String color;
    private Field currentField;
    private int points;

    //Constructor
    public Figure(String color, String type, Field currentField, int points) {
        this.color = color;
        this.type = type;
        this.currentField = currentField;
        this.points = points;
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

    public List<MoveField> getPossibleMoveFields(Board board) {
        return getPossibleFields(board).stream()
                .map(toField -> new MoveField(currentField, toField))
                .collect(Collectors.toList());
    }

}
