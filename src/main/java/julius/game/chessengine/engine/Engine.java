package julius.game.chessengine.engine;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.King;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Data
@Service
@Log4j2
public class Engine {

    private Board board = new Board();
    private Score score = new Score();

    private boolean whitesTurn = true;

    public void startNewGame() {
        board = new Board();
        whitesTurn = true;
        score = new Score();
    }

    public void moveRandomFigure(String color) {
        List<MoveField> moveFields = color.equals(Color.WHITE) ? getAllPossibleMoveFieldsForPlayerColor(Color.WHITE) :
                getAllPossibleMoveFieldsForPlayerColor(Color.BLACK);

        Random rand = new Random();
        MoveField randomMoveField = rand
                .ints(0, moveFields.size())
                .mapToObj(moveFields::get)
                .findAny().orElseThrow(() -> new RuntimeException("No random moves possible"));

        Figure randomFigure = board.getFigureForPosition(randomMoveField.getFromField().getPosition());

        log.info( randomFigure.getColor() + randomFigure.getType() +  " moves from " + randomFigure.getPosX()
                + randomFigure.getPosY() + " to " + randomMoveField.getToField().getPosition().getXAchse()
                + randomMoveField.getToField().getPosition().getYAchse());

        String fromPosition = randomFigure.getPosX() + String.valueOf(randomFigure.getPosY());
        String toPosition = randomMoveField.getToField().getPosition().getXAchse() +
                String.valueOf(randomMoveField.getToField().getPosition().getYAchse());

        moveFigure(fromPosition, toPosition);
    }

    public void moveFigure(String fromPosition, String toPosition) {
        Figure figureToMove = board.getFigureForPosition(
                new Position(fromPosition.charAt(0), Character.getNumericValue(fromPosition.charAt(1)))
        );
        Field toField = board.getFieldForPosition(
                new Position(toPosition.charAt(0), Character.getNumericValue(toPosition.charAt(1)))
        );
        if(fromPosition.length() == 2 && toPosition.length() == 2) {
            try {
                String playerColor = figureToMove.getColor();
                if (playerColor.equals(Color.WHITE) == whitesTurn && !isInStateCheckAfterMove(new MoveField(figureToMove.getCurrentField(), toField), playerColor)) {
                    if (board.isEnemyOnField(toField, playerColor)) {
                        score.add(board.getFigureForPosition(toField.getPosition()).getPoints(), playerColor);
                        figureToMove.attack(board, toField);
                    } else {
                        figureToMove.move(board, toField);
                    }
                    whitesTurn = !whitesTurn;
                    board.logBoard();
                }
            } catch (IllegalStateException e) {
                log.info(e.getMessage());
            }
        }
        else throw new RuntimeException("FromPosition " + fromPosition + " or " + "ToPosition " + toPosition + "is not valid");
    }

    public List<MoveField> getAllPossibleMoveFieldsForPlayerColor(String color) {
        return board.getFigures().stream()
                .filter(figure -> color.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleMoveFields(board).stream())
                .filter(moveField -> !isInStateCheckAfterMove(moveField, color))
                .collect(Collectors.toList());
    }

    public List<Position> getPossibleMovesForPosition(String fromPosition) {
        try {
            Figure figure = board.getFigureForPosition(
                    new Position(fromPosition.charAt(0), Character.getNumericValue(fromPosition.charAt(1)))
            );
            if(figure.getColor().equals(Color.WHITE) == whitesTurn) {
                return figure.getPossibleMoveFields(board).stream()
                        .filter(moveField -> !isInStateCheckAfterMove(moveField, figure.getColor()))
                        .map(MoveField::getToField)
                        .map(Field::getPosition)
                        .collect(Collectors.toList());
            }
            else {
                return Collections.EMPTY_LIST;
            }
        } catch (RuntimeException e) {
            log.info(e.getMessage());
            return Collections.EMPTY_LIST;
        }
    }

    private boolean isInStateCheckAfterMove(MoveField moveField, String color) {
        String fen = FEN.translateBoardToFEN(board).getRenderBoard();
        Board checkBoard = new Board(fen);
        Figure figureToMove = checkBoard.getFigureForPosition(moveField.getFromField().getPosition());
        try {
            if (checkBoard.isEnemyOnField(moveField.getToField(), color)) {
                figureToMove.attack(checkBoard, moveField.getToField());
            } else {
                figureToMove.move(checkBoard, moveField.getToField());
            }
        } catch (IllegalStateException e) {
            checkBoard.logBoard();
            log.error(figureToMove.getType() + figureToMove.getColor() + " wanted to move from " +
                    moveField.getFromField().getPosition().getXAchse() + moveField.getFromField().getPosition().getYAchse() + " to "
                    + moveField.getToField().getPosition().getXAchse() + moveField.getToField().getPosition().getYAchse()
                    + " color is " + color + " FEN = " + fen);
        }

        return checkBoard.getKings().stream().filter(king -> color.equals(king.getColor()))
                .anyMatch(King::isInStateCheck);
    }
}
