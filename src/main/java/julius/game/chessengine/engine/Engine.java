package julius.game.chessengine.engine;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.FrontendBoard;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.Figure;
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
        List<MoveField> moveFields = color.equals(Color.WHITE) ? getAllPossibleMoveFieldsWhite() : getAllPossibleMoveFieldsBlack();

        Random rand = new Random();
        MoveField randomMoveField = rand
                .ints(0, moveFields.size())
                .mapToObj(i -> moveFields.get(i))
                .findAny().orElseThrow(() -> new RuntimeException("No random moves possible"));


        /*MoveField randomMoveField = moveFields.stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("No random moves possible"));*/

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
                if (playerColor.equals(Color.WHITE) == whitesTurn) {
                    if (board.isEnemyOnField(toField, playerColor)) {
                        figureToMove.attack(board, toField);
                        score.add(board.getFigureForPosition(toField.getPosition()).getPoints(), playerColor);
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

    public List<MoveField> getAllPossibleMoveFieldsWhite() {
        return board.getFigures().stream()
                .filter(figure -> Color.WHITE.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleMoveFields(board).stream())
                .collect(Collectors.toList());
    }

    public List<MoveField> getAllPossibleMoveFieldsBlack() {
        return board.getFigures().stream()
                .filter(figure -> Color.BLACK.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleMoveFields(board).stream())
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

    public List<Position> getPossibleMovesForPosition(String fromPosition) {
        try {
            Figure figure = board.getFigureForPosition(
                    new Position(fromPosition.charAt(0), Character.getNumericValue(fromPosition.charAt(1)))
            );
            return figure.getPossibleFields(board).stream()
                    .map(Field::getPosition)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.info(e.getMessage());
            return Collections.EMPTY_LIST;
        }
    }
}
