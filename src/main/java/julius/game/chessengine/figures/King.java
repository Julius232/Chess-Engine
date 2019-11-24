package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;

import java.util.List;

public class King extends Figure {

    public King(String color, Field field) {
        super(color, "KING", field);
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
