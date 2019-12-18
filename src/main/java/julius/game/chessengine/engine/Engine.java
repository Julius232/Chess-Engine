package julius.game.chessengine.engine;

import julius.game.chessengine.utils.Color;
import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.player.PlayerBlack;
import julius.game.chessengine.player.PlayerWhite;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@Service
public class Engine {

    private final Board board;
    private boolean whitesTurn = true;

    public void startNewGame() {
        Board board = new Board();
        PlayerWhite playerWhite = new PlayerWhite();
        PlayerBlack playerBlack = new PlayerBlack();
    }

    public void moveFigure(String fromPosition, String toPosition) {
        if(fromPosition.length() == 2 && toPosition.length() == 2) {
            Figure figure = board.getFigureForPosition(new Position(fromPosition.charAt(0), fromPosition.charAt(1)));
            figure.move(board, board.getFieldForPosition(new Position(toPosition.charAt(0), toPosition.charAt(1))));
        }
        else throw new RuntimeException("FromPosition " + fromPosition + " or " + "ToPosition " + toPosition + "is not valid");
    }

    public List<Field> getAllPossibleMoveFields() {
        return board.getFigures().stream()
                .flatMap(figure -> figure.getPossibleFields(board).stream())
                .collect(Collectors.toList());
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

}
