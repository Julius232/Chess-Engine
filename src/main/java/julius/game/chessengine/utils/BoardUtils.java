package julius.game.chessengine.utils;

import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import julius.game.chessengine.figures.Figure;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//todo maybe use these functions in board.classfi!!!???
public class BoardUtils {

    public static Field getFieldForPosition(Collection<Field> fields, Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElse(new Field("offsideTheBoard", position));
    }

    public static Figure getFigureForPosition(List<Figure> figures, Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getXAchse() + position.getYAchse()));

    }

    public static boolean isEmptyField(List<Figure> figures, Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

    public static boolean isEnemyOnField(List<Figure> figures, Field field, String currentPlayerColor) {
        return figures.stream()
                .filter(figure -> field.equals(figure.getCurrentField()))
                .anyMatch(figure -> ! currentPlayerColor.equals(figure.getColor()));
    }

    public static List<Figure> hitFigureFromBoard(List<Figure> figures, Figure movingFigure, Field toField) {
        List<Figure> newFigures = figures.stream()
                .filter(figure -> !toField.equals(figure.getCurrentField()))
                .collect(Collectors.toList());
        return moveFigureToField(newFigures, movingFigure, toField);
    }

    public static List<Figure> moveFigureToField(List<Figure> figures, Figure movingFigure, Field toField) {
        for(Figure figure : figures) {
            if(movingFigure.getCurrentPosition().equals(figure.getCurrentPosition())) {
                figure.setCurrentField(toField);
            }
        }
        return figures;
    }

}
