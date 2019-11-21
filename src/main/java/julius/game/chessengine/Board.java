package julius.game.chessengine;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final Set<Field> board = new HashSet<>();

    public Board(){
        setPawns();
        setRooks();
    }

    private void setPawns() {
        for (char x = 'a'; x <= 'h'; x++) {
            this.board.add(new Field(WHITE, new Position(x, 2), new Pawn(WHITE)));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            board.add(new Field(BLACK, new Position(x, 7), new Pawn(WHITE)));
        }
    }

    private void setRooks() {
        this.board.add(new Field(WHITE, new Position('a', 1), new Rook(WHITE)));
        this.board.add(new Field(WHITE, new Position('h', 1), new Rook(WHITE)));
        this.board.add(new Field(WHITE, new Position('a', 8), new Rook(BLACK)));
        this.board.add(new Field(WHITE, new Position('h', 8), new Rook(BLACK)));
    }

    public boolean isFigureAtPosition(Position position) {
        return ! StringUtils.isEmpty(getFieldForPosition(position).getFigureTypeOnField());
    }

    public Field getFieldForPosition(Position position) {
        return board.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    public List<Figure> getFigures() {
        return board.stream()
                .filter(field -> !field.isEmptyField())
                .map(Field::getFigure)
                .collect(Collectors.toList());
    }

    public List<Pawn> getPawns(){
        return getFigures().stream()
                .filter(figure -> "PAWN".equals(figure.getType()))
                .map(figure -> (Pawn)figure)
                .collect(Collectors.toList());
    }

    public List<Pawn> getPawnsForColor(String color){
        return getPawns().stream()
                .filter(pawn -> color.equals(pawn.getType()))
                .collect(Collectors.toList());
    }

}
