package julius.game.chessengine.figures;

import julius.game.chessengine.Field;
import lombok.Data;

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
    boolean isLegalMove(Field toField) {
        return false;
    }
}
