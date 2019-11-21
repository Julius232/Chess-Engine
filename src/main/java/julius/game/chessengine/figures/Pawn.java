package julius.game.chessengine.figures;

import julius.game.chessengine.Position;
import lombok.Data;

@Data
public class Pawn extends Figure {

    private boolean hasMoved = false;

    public Pawn(String color) {
        super(color, "PAWN");
    }

    public boolean isFirstMove(){
        return true;
    }

    @Override
    void kill(Position toPosition) {

    }

    @Override
    boolean isLegalMove() {
        return false;
    }
}
