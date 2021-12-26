package julius.game.chessengine.figures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jcabi.aspects.Cacheable;
import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.MoveField;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Cacheable(lifetime = 30, unit = TimeUnit.SECONDS)
    abstract public List<Field> getPossibleFields(Board board);

    @JsonIgnore
    public Position getCurrentPosition() {
        return this.currentField.getPosition();
    }

    @JsonIgnore
    public char getPosX() {
        return getCurrentPosition().getX();
    }

    @JsonIgnore
    public int getPosY() {
        return getCurrentPosition().getY();
    }

    @JsonIgnore
    public String positionToString() {return getCurrentField().positionToString();}

    public List<MoveField> getPossibleMoveFields(Board board) {
        return getPossibleFields(board).stream()
                .map(toField -> new MoveField(currentField, toField))
                .collect(Collectors.toList());
    }

}
