package julius.game.chessengine.utils;

import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import julius.game.chessengine.figures.Figure;

import java.util.Collection;
import java.util.List;

public class BoardUtils {

    public Field getFieldForPosition(Collection<Field> fields, Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    public Figure getFigureForPosition(List<Figure> figures, Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getXAchse() + position.getYAchse()));

    }

    public boolean isEmptyField(List<Figure> figures, Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

}
