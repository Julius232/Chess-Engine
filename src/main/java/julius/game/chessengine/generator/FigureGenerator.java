package julius.game.chessengine.generator;

import julius.game.chessengine.Board;
import julius.game.chessengine.Color;
import julius.game.chessengine.Position;
import julius.game.chessengine.figures.*;

import java.util.ArrayList;
import java.util.List;

public class FigureGenerator {

    private final Board board;

    public FigureGenerator(Board board) {
        this.board = board;
    }

    public List<Figure> initializeFigures() {
        List<Figure> figures = new ArrayList<>();
        generatePawns(figures);
        generateRooks(figures);
        generateKnights(figures);
        generateBishops(figures);
        generateQueens(figures);
        generateKings(figures);
        return figures;
    }

    private void generatePawns(List<Figure> figures) {
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(Color.WHITE, board.getFieldForPosition(new Position(x, 2))));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(Color.BLACK, board.getFieldForPosition(new Position(x, 7))));
        }
    }

    private void generateRooks(List<Figure> figures) {
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('a', 1))));
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('h', 1))));
        figures.add(new Rook(Color.BLACK, board.getFieldForPosition(new Position('a', 8))));
        figures.add(new Rook(Color.BLACK, board.getFieldForPosition(new Position('h', 8))));
    }

    private void generateKnights(List<Figure> figures) {
        figures.add(new Knight(Color.WHITE, board.getFieldForPosition(new Position('b', 1))));
        figures.add(new Knight(Color.WHITE, board.getFieldForPosition(new Position('g', 1))));
        figures.add(new Knight(Color.BLACK, board.getFieldForPosition(new Position('b', 8))));
        figures.add(new Knight(Color.BLACK, board.getFieldForPosition(new Position('g', 8))));
    }

    private void generateBishops(List<Figure> figures) {
        figures.add(new Bishop(Color.WHITE, board.getFieldForPosition(new Position('c', 1))));
        figures.add(new Bishop(Color.WHITE, board.getFieldForPosition(new Position('f', 1))));
        figures.add(new Bishop(Color.BLACK, board.getFieldForPosition(new Position('c', 8))));
        figures.add(new Bishop(Color.BLACK, board.getFieldForPosition(new Position('f', 8))));
    }

    private void generateQueens(List<Figure> figures) {
        figures.add(new Queen(Color.WHITE, board.getFieldForPosition(new Position('d', 1))));
        figures.add(new Queen(Color.BLACK, board.getFieldForPosition(new Position('d', 8))));
    }

    private void generateKings(List<Figure> figures) {
        figures.add(new King(Color.WHITE, board.getFieldForPosition(new Position('e', 1))));
        figures.add(new King(Color.BLACK, board.getFieldForPosition(new Position('e', 8))));
    }

}
