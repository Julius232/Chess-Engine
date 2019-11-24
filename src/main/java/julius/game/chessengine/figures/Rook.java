package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Field;
import lombok.Data;

import java.util.List;

@Data
public class Rook extends Figure {

    public Rook(String color, Field field) {
        super(color, "ROOK", field);
    }

    @Override
    public Board move(Board board, Field toField) {
        return null;
    }

    @Override
    public Board attack(Board board, Field toField) {
        return null;
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        return null;
    }
}
