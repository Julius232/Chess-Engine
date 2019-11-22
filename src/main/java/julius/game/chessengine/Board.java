package julius.game.chessengine;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;



@Getter
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator;

    private final Collection<Field> fields;
    private Collection<Figure> figures;

    public Board(){
        this.fieldGenerator = new FieldGenerator();

        this.fields = fieldGenerator.generateFields();
        this.figures = initializeFigures();
    }

    private List<Figure> initializeFigures() {
        List<Figure> figures = new ArrayList<>();
        figures = generatePawns(figures);
        figures = generateRooks(figures);

        return figures;
    }

    private List<Figure> generatePawns(List<Figure> figures) {
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(WHITE, getFieldForPosition(new Position(x, 2))));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(BLACK, getFieldForPosition(new Position(x, 7))));
        }
        return figures;
    }

    private List<Figure> generateRooks(List<Figure> figures) {
        figures.add(new Rook(WHITE, getFieldForPosition(new Position('a', 1))));
        figures.add(new Rook(WHITE, getFieldForPosition(new Position('h', 1))));
        figures.add(new Rook(BLACK, getFieldForPosition(new Position('a', 8))));
        figures.add(new Rook(WHITE, getFieldForPosition(new Position('h', 8))));
        return figures;
    }


    public Field getFieldForPosition(Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    public Figure getFigureForPosition(Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getXAchse() + position.getYAchse()));

    }

    public boolean isEmptyField(Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

}
