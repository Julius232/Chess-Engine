package julius.game.chessengine;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;
import julius.game.chessengine.generator.FieldGenerator;
import julius.game.chessengine.generator.FigureGenerator;
import lombok.Getter;

import java.util.*;



@Getter
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator;
    private final FigureGenerator figureGenerator;

    private final Collection<Field> fields;
    private Collection<Figure> figures;

    public Board(){
        this.fieldGenerator = new FieldGenerator();
        this.fields = fieldGenerator.generateFields();

        this.figureGenerator = new FigureGenerator(fields);
        this.figures = figureGenerator.initializeFigures();
    }

}
