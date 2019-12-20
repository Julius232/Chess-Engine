package julius.game.chessengine.engine;

import julius.game.chessengine.board.FrontendBoard;
import julius.game.chessengine.figures.Knight;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.player.PlayerBlack;
import julius.game.chessengine.player.PlayerWhite;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Service
@Log4j2
public class Engine {

    private Board board = new Board();
    private boolean whitesTurn = true;

    public void startNewGame() {
        board = new Board();
        whitesTurn = true;
    }

    public void moveFigure(String fromPosition, String toPosition) {
        if(fromPosition.length() == 2 && toPosition.length() == 2) {
            Figure figure = board.getFigureForPosition(new Position(fromPosition.charAt(0), Character.getNumericValue(fromPosition.charAt(1))));
            if(figure.getColor().equals(Color.WHITE) == whitesTurn) {
                figure.move(board, board.getFieldForPosition(new Position(toPosition.charAt(0), Character.getNumericValue(toPosition.charAt(1)))));
                whitesTurn = !whitesTurn;
                board.logBoard();
            }
        }
        else throw new RuntimeException("FromPosition " + fromPosition + " or " + "ToPosition " + toPosition + "is not valid");
    }

    public List<Field> getAllPossibleMoveFieldsWhite() {
        return board.getFigures().stream()
                .filter(figure -> Color.WHITE.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleFields(board).stream())
                .collect(Collectors.toList());
    }

    public List<Field> getAllPossibleMoveFieldsBlack() {
        return board.getFigures().stream()
                .filter(figure -> Color.BLACK.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleFields(board).stream())
                .collect(Collectors.toList());
    }

    public FrontendBoard translateBoardToFrontend() {
        StringBuilder frontendBoard = new StringBuilder();
        for(int y = 8; y >= 1; y--) {
            int count = 0;
            for(char x = 'a'; x <= 'h'; x++) {
                Field f = board.getFieldForPosition(new Position(x, y));
                Field nextF = board.getFieldForPosition(new Position((char)(x + 1), y));
                if (board.isOccupiedField(f)) {
                    frontendBoard.append(convertToFrontendChar(board.getFigureForPosition(f.getPosition())));
                }
                else {
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
            if(y > 1) {
                frontendBoard.append('/');
            }
        }
        return new FrontendBoard(frontendBoard.toString());
    }

    private char convertToFrontendChar(Figure figure) {
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
