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
    private GameState gameState;
    private boolean whitesTurn = true;

    public void startNewGame() {
        board = new Board();
        whitesTurn = true;
        gameState = new GameState();
    }

    public GameState moveRandomFigure(String color) {
        List<MoveField> moveFields = getAllPossibleMoveFieldsForPlayerColor(color);

        Random rand = new Random();
        MoveField randomMoveField = rand
                .ints(0, moveFields.size())
                .mapToObj(moveFields::get)
                .findAny().orElseThrow(() -> new RuntimeException("No random moves possible"));

        Figure randomFigure = board.getFigureForPosition(randomMoveField.getFromPosition());
        String fromPosition = randomFigure.positionToString();
        String toPosition = randomMoveField.toPositionToString();

        return moveFigure(fromPosition, toPosition);
    }

    public GameState moveFigure(String fromPosition, String toPosition) {
        Figure figureToMove = board.getFigureForString(fromPosition);
        Field toField = board.getFieldForString(toPosition);
        try {
            if (isPlayersTurnAndIsNotInStateCheckAfterMove(figureToMove, toField)) {
                moveOrAttackFigure(board, figureToMove, toField);
                whitesTurn = !whitesTurn;
                board.logBoard();
            }
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        updateGameState();
        return gameState;
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
            Figure figure = board.getFigureForString(fromPosition);
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

    private boolean isInStateCheck(Board board, String color) {
        return board.getKings().stream().filter(king -> color.equals(king.getColor()))
                .anyMatch(King::isInStateCheck);
    }

    private boolean isInStateCheckAfterMove(MoveField moveField, String color) {
        Board checkBoard = generateDummyBoard(board);
        Figure figureToMove = checkBoard.getFigureForPosition(moveField.getFromPosition());
        moveOrAttackFigure(checkBoard, figureToMove, moveField.getToField());
        return isInStateCheck(checkBoard, color);
    }

    private void moveOrAttackFigure(Board board, Figure figureToMove, Field toField) {
        String color = figureToMove.getColor();
        if (board.isEnemyOnField(toField, color)) {
            board.getScore().add(board.getFigureForPosition(toField.getPosition()).getPoints(), color);
            figureToMove.attack(board, toField);
        } else {
            figureToMove.move(board, toField);
        }
    }

    private Board generateDummyBoard(Board board) {
        String fen = FEN.translateBoardToFEN(board).getRenderBoard();
        Score copyScore = new Score(board.getScore().getScoreWhite(), board.getScore().getScoreBlack());
        return new Board(fen, copyScore);
    }

    private boolean isPlayersTurnAndIsNotInStateCheckAfterMove(Figure figureToMove, Field toField) {
        String playerColor = figureToMove.getColor();
        return playerColor.equals(Color.WHITE) == whitesTurn && !isInStateCheckAfterMove(new MoveField(figureToMove.getCurrentField(), toField), playerColor);
    }

    private void updateGameState() {
        if (getAllPossibleMoveFieldsForPlayerColor(Color.WHITE).size() == 0 && isInStateCheck(board, Color.WHITE)) {
            gameState.setBlackWon(true);
        }
        else if (getAllPossibleMoveFieldsForPlayerColor(Color.BLACK).size() == 0 && isInStateCheck(board, Color.BLACK)) {
            gameState.setWhiteWon(true);
        }
        else if(getAllPossibleMoveFieldsForPlayerColor(Color.WHITE).size() == 0 || getAllPossibleMoveFieldsForPlayerColor(Color.BLACK).size() == 0) {
            gameState.setDraw(true);
        }
    }

}
