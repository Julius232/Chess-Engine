package julius.game.chessengine.figures;

import julius.game.chessengine.Position;
import lombok.Data;

@Data
public abstract class Figure {


    private String color;

    private String type;

    //Constructor
    public Figure(String color, String type) {
        this.color = color;
        this.type = type;
    }

    //Methods
    abstract void kill(Position toPosition);

    abstract boolean isLegalMove();


}
