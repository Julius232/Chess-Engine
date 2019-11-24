package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Knight extends Figure {

    public Knight(String color, Field field) {
        super(color, "KNIGHT", field);
    }

    @Override
    public Board move(Board board, Field toField) {
        return null;
    }

    @Override
    public Board attack(Board board, Field toField) {
        return null;
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        
        List<Field> attackFields = getAllPositionMoves()
                .stream()
                .filter(position -> position.isPositionInFields(board.getAllEnemyFields(getColor())))
                .map(position -> board.getFieldForPosition(position))
                .collect(Collectors.toList());

        List<Field> moveFields = getAllPositionMoves()
                .stream()
                .filter(position -> position.isPositionInFields(board.getAllEmptyFields()))
                .map(position -> board.getFieldForPosition(position))
                .collect(Collectors.toList());

        return Stream.concat(attackFields.stream(), moveFields.stream())
                .collect(Collectors.toList());
    }

    private List<Position> getAllPositionMoves() {
        char x = getPosX();
        int y = getPosY();

        Position twoForwardRight = new Position((char) (x + 1), (char)(y + 2));
        Position twoForwardLeft = new Position((char) (x - 1), (char)(y + 2));

        Position twoRightForward = new Position((char) (x + 2), (char)(y + 1));
        Position twoRightDownward = new Position((char) (x + 2), (char)(y - 1));

        Position twoDownwardRight = new Position((char) (x + 1), (char)(y - 2));
        Position twoDownwardLeft = new Position((char) (x - 1), (char)(y - 2));

        Position twoLeftForward = new Position((char) (x - 2), (char)(y + 1));
        Position twoLeftDownward = new Position((char) (x - 2), (char)(y - 1));

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
