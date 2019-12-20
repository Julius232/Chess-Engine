package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class King extends Figure {

    private boolean isInStateCheck = false;
    private boolean hasMoved = false;

    public King(String color, Field field) {
        super(color, "KING", field, 1337);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(toField::equals)) {
            board.moveFigureToField( this, toField);
            hasMoved = true;
        }
        else {
            throw new IllegalStateException("Move Operation of King from Position: " + getPosX() + getPosY() + " to position: "
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
            hasMoved = true;
        }
        else {
            throw new IllegalStateException("Attack Operation of King from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getXAchse() + toField.getPosition().getYAchse() + " was not possible." );
        }
        return board;
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

        Position up = new Position(x, y + 1);
        Position down = new Position(x, y - 1);

        Position right = new Position((char)(x + 1), y);
        Position left = new Position((char)(x - 1), y);

        Position rightUp = new Position((char)(x + 1), y + 1);
        Position rightDown = new Position((char)(x + 1), y - 1);

        Position leftUp = new Position((char)(x - 1), y + 1);
        Position leftDown = new Position((char)(x - 1), y - 1);

        return Arrays.asList(
                up,
                down,
                right,
                left,
                rightUp,
                rightDown,
                leftUp,
                leftDown
        );
    }
}
