package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Data
@Log4j2
@EqualsAndHashCode(callSuper = true)
public class Rook extends Figure {

    public Rook(String color, Field field) {
        super(color, "ROOK", field);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(toField::equals)) {
            board.moveFigureToField( this, toField);
            return board;
        }
        else {
            log.info("Move Operation of Rook from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
            return board;
        }
    }

    @Override
    public Board attack(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(toField::equals)) {
            board.hitFigureFromBoard(this, toField);
            return board;
        }
        else {
            log.info("Attack Operation of Rook from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
            return board;
        }
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        List<Field> possibleFields = new ArrayList<>();
        possibleFields.addAll(board.getPossibleFieldsYAxis(this));
        possibleFields.addAll(board.getPossibleFieldsXAxis(this));
        return possibleFields;
    }
}

