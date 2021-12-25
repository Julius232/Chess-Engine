package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.MoveField;
import julius.game.chessengine.utils.Color;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends Figure {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(King.class);
    private boolean inStateCheck;
    private boolean hasMoved;

    public King(String color, Field field, boolean hasMoved, boolean inStateCheck) {
        super(color, "KING", field, 1337);
        this.hasMoved = hasMoved;
        this.inStateCheck = inStateCheck;
    }

    @Override
    public Board move(Board board, Field toField) {
        if(getPossibleFields(board)
                .parallelStream()
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
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible." );
        }
        return board;
    }

    @Override
    public Board attack(Board board, Field toField) {
        if(getPossibleFields(board)
                .parallelStream()
                .anyMatch(toField::equals)) {
            board.hitFigureFromBoard(this, toField);
            hasMoved = true;
        }
        else {
            throw new IllegalStateException("Attack Operation of King from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible." );
        }
        return board;
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        List<Field> attackFields = getAllPositionMoves()
                .parallelStream()
                .filter(position -> position.isPositionInFields(board.getAllEnemyFields(getColor())))
                .map(board::getFieldForPosition)
                .collect(Collectors.toList());

        List<Field> moveFields = getAllPositionMoves()
                .parallelStream()
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

        return Stream.concat(attackFields.parallelStream(), moveFields.parallelStream())
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
        return (char) (currentPosition.getX() - 2) == toPosition.getX();
    }

    private boolean isKingSideCastlingMove(Field toField) {
        Position currentPosition = getCurrentPosition();
        Position toPosition = toField.getPosition();
        return (char) (currentPosition.getX() + 2) == toPosition.getX();
    }

    public boolean checkState(Board board) {
        return board.getFigures()
                .parallelStream()
                .filter(figure -> !figure.getColor().equals(getColor()))
                .flatMap(figure -> figure.getPossibleMoveFields(board).parallelStream())
                .map(MoveField::getToField)
                .anyMatch(toField -> toField.equals(getCurrentField()));
    }

    public boolean isInStateCheck() {
        return this.inStateCheck;
    }

    public boolean isHasMoved() {
        return this.hasMoved;
    }

    public void setInStateCheck(boolean inStateCheck) {
        this.inStateCheck = inStateCheck;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public String toString() {
        return "King(inStateCheck=" + this.isInStateCheck() + ", hasMoved=" + this.isHasMoved() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof King)) return false;
        final King other = (King) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.isInStateCheck() != other.isInStateCheck()) return false;
        if (this.isHasMoved() != other.isHasMoved()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof King;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + (this.isInStateCheck() ? 79 : 97);
        result = result * PRIME + (this.isHasMoved() ? 79 : 97);
        return result;
    }
}


