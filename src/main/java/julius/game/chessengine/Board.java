package julius.game.chessengine;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;
import julius.game.chessengine.generator.FieldGenerator;
import julius.game.chessengine.generator.FigureGenerator;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator;
    private final FigureGenerator figureGenerator;

    private final List<Field> fields;
    private List<Figure> figures;

    public Board(){
        this.fieldGenerator = new FieldGenerator();
        this.fields = fieldGenerator.generateFields();

        this.figureGenerator = new FigureGenerator(this);
        this.figures = figureGenerator.initializeFigures();
    }

    public Field getFieldForPosition(Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElse(new Field("offsideTheBoard", position));
    }

    public Figure getFigureForPosition(Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getXAchse() + position.getYAchse()));

    }

    public boolean isEmptyField(Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

    public boolean isEnemyOnField(Field field, String currentPlayerColor) {
        return figures.stream()
                .filter(figure -> field.equals(figure.getCurrentField()))
                .anyMatch(figure -> ! currentPlayerColor.equals(figure.getColor()));
    }

    public List<Figure> hitFigureFromBoard(Figure movingFigure, Field toField) {
        figures = figures.stream()
                .filter(figure -> !toField.equals(figure.getCurrentField()))
                .collect(Collectors.toList());
        return moveFigureToField(movingFigure, toField);
    }

    public List<Figure> moveFigureToField(Figure movingFigure, Field toField) {
        for(Figure figure : figures) {
            if(movingFigure.getCurrentPosition().equals(figure.getCurrentPosition())) {
                figure.setCurrentField(toField);
            }
        }
        return figures;
    }

    public void addFigure(Figure figure) {
        this.figures.add(figure);
    }

}
