package julius.game.chessengine.ai;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Log4j2
@Component
public class AI {

    private final Engine engine;

    public AI(Engine engine) {
        this.engine = engine;
    }

    public GameState executeCalculatedMove(String color) {
        MoveField calculatedMove = calculateMove(engine.getBoard(), color);
        return engine.moveFigure(engine.getBoard(), calculatedMove.fromPositionToString(), calculatedMove.toPositionToString());
    }

    private MoveField calculateMove(Board board, String color) {
        String fenBoard = FEN.translateBoardToFEN(board).getRenderBoard();
        int levelOfDepth = 2;
        String filePath = "src/main/resources/best.moves.level" + levelOfDepth;

        try {
            Map<String, MoveField> bestMoves = new HashMap<>();
            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));
            for (String key : properties.stringPropertyNames()) {
                bestMoves.put(key, new MoveField(board, properties.get(key).toString()));
            }
            if(bestMoves.containsKey(fenBoard)) {
                MoveField bestMove = bestMoves.get(fenBoard);
                log.info("Found bestMove for " + fenBoard + " = " + bestMove.toString() + " in " + filePath);
                return bestMove;
            }

            List<MoveField> moves = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);

            MoveField calculatedMoveField = getMaxScoreMoveOfAllPossibleMoves(board, moves, color, levelOfDepth);

            log.info("Calculated Move is From: " + calculatedMoveField.fromPositionToString()
                    + " To: " + calculatedMoveField.toPositionToString());
            bestMoves.put(FEN.translateBoardToFEN(board).getRenderBoard(), calculatedMoveField);

            for (Map.Entry<String,MoveField> entry : bestMoves.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().toString());
            }

            properties.store(new FileOutputStream(filePath), null);

            return calculatedMoveField;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;

    }

    private MoveField getMaxScoreMoveOfAllPossibleMoves(Board board, List<MoveField> moves, String color, int level) {
        double min = 3333;
        double max = -3333;
        MoveField bestMove = moves.get(0);
        for(MoveField move: moves) {
            min = getMinScoreForPredictingNextMovesAfterMove(board, move, color, min, max, level, moves.size()/20.0);
            if(min > max) {
                log.info("max was " + max + " now is " + min);
                max = min;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private double getMaxScoreOfAllPossibleMoves(Board board, List<MoveField> moves, String color, double min, double max, int level) {
        double maximum = max;
        double minimum = min;
        for(MoveField move : moves) {
            minimum = getMinScoreForPredictingNextMovesAfterMove(board, move, color, minimum, maximum, level, moves.size()/20.0);
            if(minimum > maximum) {
                log.info("booya maximum was " + maximum + " now is " + minimum);
                maximum = minimum;
            }
        }
        return maximum;
    }

    private double getMinScoreForPredictingNextMovesAfterMove(Board board, MoveField move, String color, double min, double max, int level, double sizeOfMoves) {
       Board boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
       double minScore = min;
       log.info("Level" + level);

       double scoreAfterFirstMove = boardAfterMove.getScore().getScoreDifference(color) + sizeOfMoves;
       if(scoreAfterFirstMove >= max) {
           List<MoveField> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterMove, Color.getOpponentColor(color));
           for (MoveField opponentMove : opponentMoves) {
               Board boardAfterSecondMove = engine.simulateMoveAndGetDummyBoard(boardAfterMove, opponentMove);
               double scoreAfterSecondMove = boardAfterSecondMove.getScore().getScoreDifference(color) + sizeOfMoves;
               if (scoreAfterSecondMove < minScore && scoreAfterSecondMove >= max) {
                   minScore = scoreAfterSecondMove;
                   if(level > 1) {
                       List<MoveField> nMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterSecondMove, color);;
                       log.info("minscore: " + minScore + "going into recursion");
                       minScore = getMaxScoreOfAllPossibleMoves(boardAfterSecondMove, nMoves, color, minScore, max, level - 1);
                   }
                   //boardAfterSecondMove.logBoard();
               }
           }
           if(level <= 1) {
               log.info("minscore: " + minScore + " maxscore: " + max + " " + move.toString());
               return minScore;
           }
       }
       log.info("scoreAfterFirstMove: " + scoreAfterFirstMove + " " + move.toString());
       return scoreAfterFirstMove;
    }
}
