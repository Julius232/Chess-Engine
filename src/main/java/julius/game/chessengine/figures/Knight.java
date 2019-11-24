package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Field;

import java.util.List;

public class Knight extends Figure {

    public Knight(String color, Field field) {
        super(color, "KNIGHT", field);
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
