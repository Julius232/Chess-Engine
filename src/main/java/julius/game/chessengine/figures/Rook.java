package julius.game.chessengine.figures;

import julius.game.chessengine.Position;
import lombok.Data;

@Data
public class Rook extends Figure {

    public Rook(String color) {
        super(color, "ROOK");
    }

    @Override
    void kill(Position toPosition) {

    }

    @Override
    boolean isLegalMove() {
        return false;
    }
}
