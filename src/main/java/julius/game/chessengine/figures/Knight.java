package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class Knight extends Figure {

    public Knight(String color, Field field) {
        super(color, "KNIGHT", field);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(toField::equals)) {
            board.moveFigureToField( this, toField);
        }
        else {
            log.info("Move Operation of Knight from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
        }
        return board;
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
            log.info("Attack Operation of Knight from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
            return board;
        }
    }

    @Override
    public List<Field> getPossibleFields(Board board) {

        List<Field> attackFields = getAllPositionMoves()
                .stream()
                .filter(position -> position.isPositionInFields(board.getAllEnemyFields(getColor())))
                .map(board::getFieldForPosition)
                .collect(Collectors.toList());

        List<Field> moveFields = getAllPositionMoves()
                .stream()
                .filter(position -> position.isPositionInFields(board.getAllEmptyFields()))
                .map(board::getFieldForPosition)
                .collect(Collectors.toList());

        return Stream.concat(attackFields.stream(), moveFields.stream())
                .collect(Collectors.toList());
    }

    private List<Position> getAllPositionMoves() {
        char x = getPosX();
        int y = getPosY();

        Position twoForwardRight = new Position((char) (x + 1), y + 2);
        Position twoForwardLeft = new Position((char) (x - 1), y + 2);

        Position twoRightForward = new Position((char) (x + 2), y + 1);
        Position twoRightDownward = new Position((char) (x + 2), y - 1);

        Position twoDownwardRight = new Position((char) (x + 1), y - 2);
        Position twoDownwardLeft = new Position((char) (x - 1), y - 2);

        Position twoLeftForward = new Position((char) (x - 2), y + 1);
        Position twoLeftDownward = new Position((char) (x - 2), y - 1);

        return Arrays.asList(
                twoForwardRight,
                twoForwardLeft,
                twoRightForward,
                twoRightDownward,
                twoDownwardRight,
                twoDownwardLeft,
                twoLeftForward,
                twoLeftDownward
        );
    }
}
