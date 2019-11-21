package julius.game.chessengine.figures;

import julius.game.chessengine.Position;
import lombok.Data;

@Data
public class Pawn extends Figure {

    private boolean hasMoved = false;

    public Pawn(String color) {
        super(color, "PAWN");
    }

    public moveForward(Position position) {
        if("White".equals(super.getColor())) {
            position
        }
    }

    @Override
    Position availableMoves() {
        return null;
    }

    @Override
    Position availableKills() {
        return null;
    }

    @Override
    boolean isLegalMove(){
        return true;
    }

    public boolean isFirstMove(){
        return true;
    }
}
