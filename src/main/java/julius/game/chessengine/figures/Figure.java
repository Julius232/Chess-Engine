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

    //GETTER && SETTER
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    //Methods
    abstract void kill(Position toPosition);

    abstract Position availableMoves();

    abstract Position availableKills();

    abstract boolean isLegalMove();


}
