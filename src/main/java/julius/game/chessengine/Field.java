package julius.game.chessengine;


import julius.game.chessengine.figures.Figure;
import lombok.Data;

@Data
public class Field {

    private final Position position;

    private Figure figure;

    private final String color;

    public Field(String color, Position position, Figure figure) {
        this.color = color;
        this.position = position;
        this.figure = figure;
    }

    public String getFigureTypeOnField() {
        if (figure.getType() == null) {
            return "";
        }
        else return figure.getType();
    }

    public boolean isEmptyField() {
       return figure == null ? true : false;
    }
}
