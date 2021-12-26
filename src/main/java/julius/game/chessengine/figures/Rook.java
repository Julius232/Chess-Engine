package julius.game.chessengine.figures;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Figure {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Rook.class);
    private boolean hasMoved;

    public Rook(String color, Field field, boolean hasMoved) {
        super(color, "ROOK", field, 5);
        this.hasMoved = hasMoved;
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
            throw new IllegalStateException("Move Operation of Rook from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible." );
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
            throw new IllegalStateException("Attack Operation of Rook from Position: " + getPosX() + getPosY() + " to position: "
                    + toField.getPosition().getX() + toField.getPosition().getY() + " was not possible." );
        }
        return board;
    }

    @Override
    public List<Field> getPossibleFields(Board board) {
        List<Field> possibleFields = new ArrayList<>();
        possibleFields.addAll(board.getPossibleFieldsYAxis(this));
        possibleFields.addAll(board.getPossibleFieldsXAxis(this));
        return possibleFields;
    }

    public boolean isHasMoved() {
        return this.hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public String toString() {
        return "Rook(hasMoved=" + this.isHasMoved() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Rook)) return false;
        final Rook other = (Rook) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.isHasMoved() != other.isHasMoved()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Rook;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + (this.isHasMoved() ? 79 : 97);
        return result;
    }
}

