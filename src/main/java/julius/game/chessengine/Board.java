package julius.game.chessengine;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;
import julius.game.chessengine.generator.FieldGenerator;
import julius.game.chessengine.generator.FigureGenerator;
import lombok.Data;
import lombok.Getter;

import java.util.*;

@Data
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator;
    private final FigureGenerator figureGenerator;

    private final List<Field> fields;
    private List<Figure> figures;

    public Board(){
        this.fieldGenerator = new FieldGenerator();
        this.fields = fieldGenerator.generateFields();

        this.figureGenerator = new FigureGenerator(fields);
        this.figures = figureGenerator.initializeFigures();
    }

}
