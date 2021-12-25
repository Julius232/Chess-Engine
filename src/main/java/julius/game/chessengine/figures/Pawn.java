package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.utils.Color;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Figure {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Pawn.class);
    private boolean hasMoved;

    public Pawn(String color, Field field, boolean hasMoved) {
        super(color, "PAWN", field, 1);
        this.hasMoved = hasMoved;
    }

    @Override
    public Board move(Board board, Field toField) {
        if (getPossibleFields(board)
                .parallelStream()
                .anyMatch(toField::equals)) {
            board.moveFigureToField(this, toField);
            setHasMoved(true);
        } else {
            throw new IllegalStateException("Move Operation of Pawn from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible.");
        }
        return board;
    }

    @Override
    public Board attack(Board board, Field toField) {
        if (getPossibleFields(board)
                .parallelStream()
                .anyMatch(toField::equals)) {
            board.hitFigureFromBoard(this, toField);
            setHasMoved(true);
        } else {
            throw new IllegalStateException("Attack Operation of Pawn from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible.");
        }
        return board;
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
        } else if (Color.BLACK.equals(pawnColor)) {
            moveOneForward = getPosY() - 1;
            moveTwoForward = getPosY() - 2;
        } else throw new RuntimeException(String.format("Color %s is not a valid color.", pawnColor));

        Field attackLeft = board.getFieldForPosition(
                new Position((char) (getPosX() - 1), moveOneForward)
        );

        Field attackRight = board.getFieldForPosition(
                new Position((char) (getPosX() + 1), moveOneForward)
        );

        if (Color.WHITE.equals(pawnColor) && moveOneForward <= 8 || Color.BLACK.equals(pawnColor) && moveOneForward >= 1) {
            Field field = board.getFieldForPosition(new Position(getPosX(), moveOneForward));
            if (board.isEmptyField(field)) {
                possibleFields.add(field);
            }
        }

        if (!hasMoved && board.isEmptyField(board.getFieldForPosition(new Position(getPosX(), moveOneForward)))) {
            Field field = board.getFieldForPosition(new Position(getPosX(), moveTwoForward));
            if (board.isEmptyField(field)) {
                possibleFields.add(field);
            }
        }

        if (board.isEnemyOnField(attackLeft, pawnColor)) {
            possibleFields.add(attackLeft);
        }

        if (board.isEnemyOnField(attackRight, pawnColor)) {
            possibleFields.add(attackRight);
        }

        return possibleFields;
    }

    public boolean isHasMoved() {
        return this.hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public String toString() {
        return "Pawn(hasMoved=" + this.isHasMoved() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Pawn)) return false;
        final Pawn other = (Pawn) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.isHasMoved() != other.isHasMoved()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Pawn;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + (this.isHasMoved() ? 79 : 97);
        return result;
    }
}

