package julius.game.chessengine;


import julius.game.chessengine.figures.Figure;
import lombok.Data;

import java.util.Optional;

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

    public String getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public Optional<Figure> getFigure() {
        return Optional.ofNullable(this.figure);
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public String getFigureTypeOnField() {
        if (figure.getType() == null) {
            return "";
        }
        else return figure.getType();
    }
}
