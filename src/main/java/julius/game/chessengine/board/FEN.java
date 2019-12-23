package julius.game.chessengine.board;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.utils.Color;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FEN {
    private final String renderBoard;

    public static FEN translateBoardToFEN(Board board) {
        StringBuilder frontendBoard = new StringBuilder();
        for (int y = 8; y >= 1; y--) {
            int count = 0;
            for (char x = 'a'; x <= 'h'; x++) {
                Field f = board.getFieldForPosition(new Position(x, y));
                Field nextF = board.getFieldForPosition(new Position((char) (x + 1), y));
                if (board.isOccupiedField(f)) {
                    frontendBoard.append(convertToFrontendChar(board.getFigureForPosition(f.getPosition())));
                } else {
                    ++count;
                    if (board.isOccupiedField(nextF)) {
                        frontendBoard.append(count);
                        count = 0;
                    }
                }
                if (x == 'h' && count > 0) {
                    frontendBoard.append(count);
                }
            }
            if (y > 1) {
                frontendBoard.append('/');
            }
        }
        return new FEN(frontendBoard.toString());
    }

    private static char convertToFrontendChar(Figure figure) {
        String type = figure.getType();
        String color = figure.getColor();
        char frontendChar = type.charAt(0);
        if(type.equals("KNIGHT")) {
            frontendChar = 'N';
        }
        if(color.equals(Color.BLACK)) {
            frontendChar = Character.toLowerCase(frontendChar);
        }
        return frontendChar;
    }
}