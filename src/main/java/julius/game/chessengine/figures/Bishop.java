package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Bishop extends Figure {

    public Bishop(String color, Field field) {
        super(color, "BISHOP", field, 3);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .parallelStream()
                .anyMatch(toField::equals)) {
            board.moveFigureToField( this, toField);
        }
        else {
            throw new IllegalStateException("Move Operation of Bishop from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible.");
        }
        return board;
    }

    @Override
    public Board attack(Board board, Field toField) {
        if(getPossibleFields(board)
                .parallelStream()
                .anyMatch(toField::equals)) {
            board.hitFigureFromBoard(this, toField);
        }
        else {
            throw new IllegalStateException("Attack Operation of Bishop from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible." );
        }
        return board;
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        List<Field> possibleFields = new ArrayList<>();
        possibleFields.addAll(board.getPossibleFieldsDiagonalLDRU(this));
        possibleFields.addAll(board.getPossibleFieldsDiagonalRDLU(this));
        return possibleFields;
    }
}
