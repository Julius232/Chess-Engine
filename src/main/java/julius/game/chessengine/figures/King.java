package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.utils.Color;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Log4j2
@EqualsAndHashCode(callSuper = true)
public class King extends Figure {

    private boolean inStateCheck = false;
    private boolean hasMoved = false;

    public King(String color, Field field) {
        super(color, "KING", field, 1337);
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .stream()
                .anyMatch(toField::equals)) {
            if(isQueenSideCastlingMove(toField)) {
                int yAxis = this.getColor().equals(Color.WHITE) ? 1 : 8;
                board.moveFigureToField(this, toField);
                board.moveFigureToField(board.getFigureForPosition(new Position('a', yAxis)),
                        board.getFieldForPosition(new Position('d', yAxis))
                );
            }
            else if(isKingSideCastlingMove(toField)) {
                int yAxis = this.getColor().equals(Color.WHITE) ? 1 : 8;
                board.moveFigureToField(this, toField);
                board.moveFigureToField(board.getFigureForPosition(new Position('h', yAxis)),
                        board.getFieldForPosition(new Position('f', yAxis))
                );
            }
            else {
                board.moveFigureToField(this, toField);
            }
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

        if(board.isCastlingPossibleKingSide(this.getColor())) {
            Field castlingKingSide = board.getFieldForPosition(new Position((char)(this.getPosX() + 2), this.getPosY()));
            moveFields.add(castlingKingSide);
        }

        if(board.isCastlingPossibleQueenSide(this.getColor())) {
            Field castlingQueenSide = board.getFieldForPosition(new Position((char)(this.getPosX() - 2), this.getPosY()));
            moveFields.add(castlingQueenSide);
        }

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

    private boolean isQueenSideCastlingMove(Field toField) {
        Position currentPosition = getCurrentPosition();
        Position toPosition = toField.getPosition();
        return (char) (currentPosition.getXAchse() - 2) == toPosition.getXAchse();
    }

    private boolean isKingSideCastlingMove(Field toField) {
        Position currentPosition = getCurrentPosition();
        Position toPosition = toField.getPosition();
        return (char) (currentPosition.getXAchse() + 2) == toPosition.getXAchse();
    }
}


