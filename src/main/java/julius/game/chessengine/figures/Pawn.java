package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Data
@Log4j2
public class Pawn extends Figure {

    private boolean hasMoved = false;

    public Pawn(String color, Field field) {
        super(color, "PAWN", field);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(field -> toField.equals(field))) {
            board.moveFigureToField( this, toField);
            setHasMoved(true);
            return board;
        }
        else {
            log.info("Move Operation of Pawn from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
            return board;
        }
    }

    @Override
    public Board attack(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(field -> toField.equals(field))) {
            board.hitFigureFromBoard(this, toField);
            setHasMoved(true);
            return board;
        }
        else {
            log.info("Attack Operation of Pawn from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
            return board;
        }
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        List<Field> possibleFields = new ArrayList<>();
        String pawnColor = super.getColor();

        int moveOneForward;
        int moveTwoForward;

        if (Color.WHITE.equals(pawnColor)) {
            moveOneForward = getPosY() + 1;
            moveTwoForward = getPosY() + 2;
        }
        else if (Color.BLACK.equals(pawnColor)) {
            moveOneForward = getPosY() + 1;
            moveTwoForward = getPosY() + 2;
        }

        else throw new RuntimeException(String.format("Color %s is not a valid color.", pawnColor));

        Field attackLeft = board.getFieldForPosition(
                new Position((char) (getPosX() - 1), moveOneForward)
        );

        Field attackRight = board.getFieldForPosition(
                new Position((char) (getPosX() + 1), moveTwoForward)
        );

        if (Color.WHITE.equals(pawnColor) && moveOneForward <= 8 || Color.BLACK.equals(pawnColor) && moveOneForward >= 1) {
            Field field = board.getFieldForPosition(new Position(getPosX(), moveOneForward));
            if (board.isEmptyField(field)) {
                possibleFields.add(field);
            }
        }

        if (!hasMoved) {
            Field field = board.getFieldForPosition(new Position(getPosX(), moveTwoForward));
            if (board.isEmptyField(field)) {
                possibleFields.add(field);
            }
        }

        if(board.isEnemyOnField(attackLeft, pawnColor)) {
            possibleFields.add(attackLeft);
        }

        if(board.isEnemyOnField(attackRight, pawnColor)) {
            possibleFields.add(attackRight);
        }

    return possibleFields;
    }

}
