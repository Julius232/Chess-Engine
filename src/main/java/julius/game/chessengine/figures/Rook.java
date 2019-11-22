package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Field;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class Rook extends Figure {

    public Rook(String color, Field field) {
        super(color, "ROOK", field);
    }

    @Override
    void move(Field toField) {

    }

    @Override
    void kill(Field toField) {

    }

    @Override
    List<Field> getPossibleFields(Board board) {
        return null;
    }

    @Override
    boolean isLegalMove(Field toField) {
        return false;
    }
}
