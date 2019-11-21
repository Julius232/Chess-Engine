package julius.game.chessengine;

import julius.game.chessengine.figures.Pawn;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final Set<Field> board = new HashSet<>();

    public Board(){
        setPawns();
    }

    private void setPawns() {
        for (char x = 'a'; x <= 'h'; x++) {
            this.board.add(new Field(WHITE, new Position(x, 2), new Pawn(WHITE)));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            board.add(new Field(BLACK, new Position(x, 7), new Pawn(WHITE)));
        }
    }

    public boolean isFigureOnField(Position position) {
        return ! StringUtils.isEmpty(getFieldForPosition(position).getFigureTypeOnField());
    }

    public Field getFieldForPosition(Position position) {
        return board.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }



}
