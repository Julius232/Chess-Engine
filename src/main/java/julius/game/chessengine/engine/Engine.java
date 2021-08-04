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
    private GameState gameState = new GameState();
    private boolean whitesTurn = true;

    public void startNewGame() {
        board = new Board();
        whitesTurn = true;
        gameState = new GameState();
    }

    public GameState moveRandomFigure(String color) {
        List<MoveField> moveFields = getAllPossibleMoveFieldsForPlayerColor(board, color);

        Random rand = new Random();
        MoveField randomMoveField = rand
                .ints(0, moveFields.size())
                .mapToObj(moveFields::get)
                .findAny().orElseThrow(() -> new RuntimeException("No random moves possible"));

        Figure randomFigure = board.getFigureForPosition(randomMoveField.getFromPosition());
        String fromPosition = randomFigure.positionToString();
        String toPosition = randomMoveField.toPositionToString();

        return moveFigure(board, fromPosition, toPosition);
    }

    public GameState moveFigure(Board board, String fromPosition, String toPosition) {
        Figure figureToMove = board.getFigureForString(fromPosition);
        Field toField = board.getFieldForString(toPosition);
        try {
            if (isPlayersTurnAndIsNotInStateCheckAfterMove(board, figureToMove, toField)) {
                moveOrAttackFigure(board, figureToMove, toField);
                whitesTurn = !whitesTurn;
                //board.logBoard();
            }
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
        }
        updateGameState();
        return gameState;
    }

    public List<MoveField> getAllPossibleMoveFieldsForPlayerColor(Board board, String color) {
        return board.getFigures().parallelStream()
                .filter(figure -> color.equals(figure.getColor()))
                .flatMap(figure -> figure.getPossibleMoveFields(board).stream())
                .filter(moveField -> !isInStateCheckAfterMove(board, moveField, color))
                .collect(Collectors.toList());
    }

    public List<Position> getPossibleMovesForPosition(Board board, String fromPosition) {
        try {
            Figure figure = board.getFigureForString(fromPosition);
            if(figure.getColor().equals(Color.WHITE) == whitesTurn) {
                return figure.getPossibleMoveFields(board).parallelStream()
                        .filter(moveField -> !isInStateCheckAfterMove(board, moveField, figure.getColor()))
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
        return board.getKings().parallelStream().filter(king -> color.equals(king.getColor()))
                .anyMatch(King::isInStateCheck);
    }

    public double simulateMoveAndGetEfficiency(Board board, MoveField moveField, String currentPlayerColor, String color, int levelOfDepth, double mostEfficientMove) {
        double efficiency = 0;
        int iteration = 0;

        Board simulatedBoard = simulateMoveAndGetDummyBoard(board, moveField);
        int scoreDifference = simulatedBoard.getScore().getScoreDifference(color);
        efficiency += scoreDifference;

        while (iteration < levelOfDepth && efficiency > mostEfficientMove) {


            //log.info("Iteration: (" + (iteration + 1) + "/" + levelOfDepth + ")");
            double mostEfficientMoveScoreDifference =
                    getMostEfficientMoveScoreDifference(Color.getOpponentColor(color), levelOfDepth, currentPlayerColor, simulatedBoard, efficiency);

            if (Color.getOpponentColor(color).equals(currentPlayerColor)) {
                efficiency += mostEfficientMoveScoreDifference;
            } else {
                efficiency -= mostEfficientMoveScoreDifference;
            }
            iteration++;
        }

        /*log.info("Efficiency: " + efficiency + " From: " + moveField.fromPositionToString() + " To: " + moveField.toPositionToString() + " Player: " + color +
                " MostEfficientMove: " + mostEfficientMove);*/

        return efficiency;
    }

    private double getMostEfficientMoveScoreDifference(String color, int levelOfDepth, String currentPlayerColor, Board simulatedBoard, double mostEfficientMove) {
        List<MoveField> opponentMoves = getAllPossibleMoveFieldsForPlayerColor(simulatedBoard, color);
        return opponentMoves.parallelStream()
                        .mapToDouble(move -> simulateMoveAndGetEfficiency(simulatedBoard, move, currentPlayerColor, color, levelOfDepth-1, mostEfficientMove))
                        .max().orElseThrow(() -> new RuntimeException("No moves available for " + color));
    }


    public Board simulateMoveAndGetDummyBoard(Board board, MoveField moveField) {
        Board dummyBoard = generateDummyBoard(board);
        Figure figureToMove = dummyBoard.getFigureForPosition(moveField.getFromPosition());
        moveOrAttackFigure(dummyBoard, figureToMove, moveField.getToField());
        return dummyBoard;
    }

    private boolean isInStateCheckAfterMove(Board board, MoveField moveField, String color) {
        Board checkBoard = simulateMoveAndGetDummyBoard(board, moveField);
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

    private boolean isPlayersTurnAndIsNotInStateCheckAfterMove(Board board, Figure figureToMove, Field toField) {
        String playerColor = figureToMove.getColor();
        return playerColor.equals(Color.WHITE) == whitesTurn && !isInStateCheckAfterMove(board, new MoveField(figureToMove.getCurrentField(), toField), playerColor);
    }

    private void updateGameState() {
        if (getAllPossibleMoveFieldsForPlayerColor(board, Color.WHITE).size() == 0 && isInStateCheck(board, Color.WHITE)) {
            gameState.setState("BLACK WON");
        }
        else if (getAllPossibleMoveFieldsForPlayerColor(board, Color.BLACK).size() == 0 && isInStateCheck(board, Color.BLACK)) {
            gameState.setState("WHITE WON");
        }
        else if(getAllPossibleMoveFieldsForPlayerColor(board, Color.WHITE).size() == 0 || getAllPossibleMoveFieldsForPlayerColor(board, Color.BLACK).size() == 0) {
            gameState.setState("DRAW");
        }
    }

}
