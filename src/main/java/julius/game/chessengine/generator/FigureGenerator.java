package julius.game.chessengine.generator;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.board.Position;
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
            figures.add(new Pawn(Color.WHITE, board.getFieldForPosition(new Position(x, 2)), false));
        }
        for (char x = 'a'; x <= 'h'; x++) {
            figures.add(new Pawn(Color.BLACK, board.getFieldForPosition(new Position(x, 7)), false));
        }
    }

    private void generateRooks(List<Figure> figures) {
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('a', 1)), false));
        figures.add(new Rook(Color.WHITE, board.getFieldForPosition(new Position('h', 1)), false));
        figures.add(new Rook(Color.BLACK, board.getFieldForPosition(new Position('a', 8)), false));
        figures.add(new Rook(Color.BLACK, board.getFieldForPosition(new Position('h', 8)), false));
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
        figures.add(new King(Color.WHITE, board.getFieldForPosition(new Position('e', 1)), false, false));
        figures.add(new King(Color.BLACK, board.getFieldForPosition(new Position('e', 8)), false, false));
    }

    /*public List<Figure> getFigures(String FEN) {
        List<Figure> figures = new ArrayList<>();
        String trimFEN = FEN.replace("/", "");
        int count = 0;
        for(int y = 8; y >= 1; y--) {
            for(char x = 'a'; x <= 'h'; x++) {
                Field f = board.getFieldForPosition(new Position(x, y));
                if (Character.isLetter((trimFEN.charAt(count)))) {
                    figures.add(convertToFigure(trimFEN.charAt(count), f));
                }
                else {
                    x += Character.getNumericValue(trimFEN.charAt(count) - 1);
                }
                ++count;
            }
        }
        return figures;

    }*/

    /*private Figure convertToFigure(char c, Field f) {
        String color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
        switch (Character.toUpperCase(c)) {
            case 'P':
                return new Pawn(color, f);

            case 'R':
                return new Rook(color, f);

            case 'N':
                return new Knight(color, f);

            case 'B':
                return new Bishop(color, f);

            case 'Q':
                return new Queen(color, f);

            case 'K':
                return new King(color, f);

            default:
                throw new RuntimeException("Invalid FEN");
        }
    }*/

}
