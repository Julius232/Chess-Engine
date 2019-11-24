package julius.game.chessengine.figures;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Field;
import julius.game.chessengine.Position;
import julius.game.chessengine.utils.BoardUtils;
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
            board.setFigures(BoardUtils.moveFigureToField(board.getFigures(), this, toField));
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
            board.setFigures(BoardUtils.hitFigureFromBoard(board.getFigures(), this, toField));
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

        if (Color.WHITE.equals(super.getColor())) {

            Field attackLeft = BoardUtils.getFieldForPosition(
                    board.getFields(),
                    new Position((char) (getPosX() - 1), getPosY() + 1)
            );

            Field attackRight = BoardUtils.getFieldForPosition(
                    board.getFields(),
                    new Position((char) (getPosX() + 1), getPosY() + 1)
            );

            if (getPosY() + 1 <= 8) {
                Field field = BoardUtils.getFieldForPosition(board.getFields(), new Position(getPosX(), getPosY() + 1));
                if (BoardUtils.isEmptyField(board.getFigures(), field)) {
                    possibleFields.add(field);
                }
            }

            if (!hasMoved) {
                Field field = BoardUtils.getFieldForPosition(board.getFields(), new Position(getPosX(), getPosY() + 2));
                if (BoardUtils.isEmptyField(board.getFigures(), field)) {
                    possibleFields.add(field);
                }
            }

            if(BoardUtils.isEnemyOnField(board.getFigures(), attackLeft, Color.WHITE)) {
                possibleFields.add(attackLeft);
            }

            if(BoardUtils.isEnemyOnField(board.getFigures(), attackRight, Color.WHITE)) {
                possibleFields.add(attackRight);
            }
        }

        if (Color.BLACK.equals(super.getColor())) {

            Field attackLeft = BoardUtils.getFieldForPosition(
                    board.getFields(),
                    new Position((char) (getPosX() - 1), getPosY() - 1)
            );

            Field attackRight = BoardUtils.getFieldForPosition(
                    board.getFields(),
                    new Position((char) (getPosX() + 1), getPosY() - 1)
            );

            if (getPosY() - 1 >= 1) {
                Field field = BoardUtils.getFieldForPosition(board.getFields(), new Position(getPosX(), getPosY() - 1));
                if (BoardUtils.isEmptyField(board.getFigures(), field)) {
                    possibleFields.add(field);
                }
            }

            if (!hasMoved) {
                Field field = BoardUtils.getFieldForPosition(board.getFields(), new Position(getPosX(), getPosY() - 2));
                if (BoardUtils.isEmptyField(board.getFigures(), field)) {
                    possibleFields.add(field);
                }
            }

            if(BoardUtils.isEnemyOnField(board.getFigures(), attackLeft, Color.BLACK)) {
                possibleFields.add(attackLeft);
            }

            if(BoardUtils.isEnemyOnField(board.getFigures(), attackRight, Color.BLACK)) {
                possibleFields.add(attackRight);
            }
        }
        return possibleFields;
    }

}
