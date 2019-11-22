package julius.game.chessengine.figures;

import julius.game.chessengine.Field;
import lombok.Data;

@Data
public class Pawn extends Figure {

    private boolean hasMoved = false;
    private Pawn pawn;

    public Pawn(String color, Field field) {
        super(color, "PAWN", field);
    }

    @Override
    void move(Field toField) {
        if(isLegalMove(toField)) {
            setCurrentField(toField);
            setHasMoved(true);
        }
    }

    @Override
    void kill(Field toField) {
        if(isLegalMove(toField)) {

            setHasMoved(true);
        }
    }

    @Override
    boolean isLegalMove(Field toField) {
        return true;
    }
}
