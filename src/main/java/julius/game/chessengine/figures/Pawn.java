package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import lombok.Data;

import java.util.*;

@Data
public class Pawn extends Figure {

    private boolean hasMoved = false;

    public Pawn(String color, Field field) {
        super(color, "PAWN", field);
    }

    @Override
    void move(Field toField) {
        if(isLegalMove(toField)) {
            setCurrentField(toField);
            setHasMoved(true);
        }
    }

    @Override
    void kill(Field toField) {
        if(isLegalMove(toField)) {

            setHasMoved(true);
        }
    }

    @Override
    List<Field> getPossibleFields(Board board) {
        String color = super.getColor();
        List<Field> possibleFields = new ArrayList<>();

        if(Color.WHITE.equals(color) && (getPosY() + 1 <= 8)) {
            Optional<Field> possibleField = board.getFields().stream()
                    .filter(field -> field.getPosition().equals(new Position(getPosX(), getPosY() + 1)))
                    .filter(field -> board.getFigures()
                            .stream()
                            .noneMatch(figure -> field.equals(figure.getCurrentField())))
                    .findFirst();
            if(possibleField.isPresent()) {
                possibleFields.add(possibleField.get());
            }

        }

        if(!hasMoved && Color.WHITE.equals(color) && (getPosY() + 2 <= 8)) {
            Optional<Field> possibleField = board.getFields().stream()
                    .filter(field -> field.getPosition().equals(new Position(getPosX(), getPosY() + 2)))
                    .filter(field -> board.getFigures()
                            .stream()
                            .noneMatch(figure -> field.equals(figure.getCurrentField())))
                    .findFirst();
            if(possibleField.isPresent()) {
                possibleFields.add(possibleField.get());
            }

        }
        return possibleFields;
    }

    @Override
    boolean isLegalMove(Field toField) {
        return true;
    }
}
