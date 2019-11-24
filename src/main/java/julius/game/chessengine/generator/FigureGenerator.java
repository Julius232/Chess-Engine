package julius.game.chessengine.generator;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Position;
import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.Pawn;
import julius.game.chessengine.figures.Rook;

import java.util.ArrayList;
import java.util.List;

public class FigureGenerator {

    private final Board board;

    public FigureGenerator(Board board) {
        this.board = board;
    }

    public List<Figure> initializeFigures() {
        List<Figure> figures = new ArrayList<>();
        figures = generatePawns(figures);
        figures = generateRooks(figures);

        return figures;
    }

    public List<Figure> generatePawns(List<Figure> figures) {
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(Color.WHITE, board.getFieldForPosition(new Position(x, 2))));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(Color.BLACK, board.getFieldForPosition(new Position(x, 7))));
        }
        return figures;
    }

    public List<Figure> generateRooks(List<Figure> figures) {
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('a', 1))));
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('h', 1))));
        figures.add(new Rook(Color.BLACK, board.getFieldForPosition(new Position('a', 8))));
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('h', 8))));
        return figures;
    }

}
