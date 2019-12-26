package julius.game.chessengine.ai;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

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
        double max = -3333;
        MoveField bestMove = moves.get(0);
        for(MoveField move: moves) {
            double min = getMinScoreForPredictingNextMovesAfterMove(board, move, color, max, level);
            if(min > max) {
                max = min;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private double getMinScoreForPredictingNextMovesAfterMove(Board board, MoveField move, String color, double max, int level) {
       Board boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
       double minScore = 3333;

       double scoreAfterFirstMove = boardAfterMove.getScore().getScoreDifference(color);
       if(scoreAfterFirstMove > max) {
           List<MoveField> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterMove, Color.getOpponentColor(color));
           for (MoveField opponentMove : opponentMoves) {
               Board boardAfterSecondMove = engine.simulateMoveAndGetDummyBoard(boardAfterMove, opponentMove);
               double scoreAfterSecondMove = boardAfterSecondMove.getScore().getScoreDifference(color);
               if (scoreAfterSecondMove < minScore) {
                   minScore = scoreAfterSecondMove;
               }
           }

           return minScore;
       }
       return scoreAfterFirstMove;
    }
}
