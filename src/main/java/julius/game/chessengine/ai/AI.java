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
import java.util.*;
import java.util.concurrent.TimeUnit;
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

        if (calculatedMove != null) {
            return engine.moveFigure(engine.getBoard(), calculatedMove.fromPositionToString(), calculatedMove.toPositionToString());
        } else return engine.getGameState();
    }

    private MoveField calculateMove(Board board, String color) {
        //String fenBoard = FEN.translateBoardToFEN(board).getRenderBoard();
        int levelOfDepth = 1;
        String filePath = "src/main/resources/best.moves.level" + levelOfDepth;

        try {
            Map<String, MoveField> bestMoves = new HashMap<>();

            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));
            /*
            for (String key : properties.stringPropertyNames()) {
                bestMoves.put(key, new MoveField(board, properties.get(key).toString()));
            }
            if (bestMoves.containsKey(fenBoard)) {
                MoveField bestMove = bestMoves.get(fenBoard);
                log.info("Found bestMove for " + fenBoard + " = " + bestMove.toString() + " in " + filePath);
                return bestMove;
            }*/

            List<MoveField> moves = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);

            MoveField calculatedMoveField = getMaxScoreMoveOfAllPossibleMoves(board, moves, color, levelOfDepth);

            log.info("Calculated Move is From: " + calculatedMoveField.fromPositionToString()
                    + " To: " + calculatedMoveField.toPositionToString());
            //bestMoves.put(FEN.translateBoardToFEN(board).getRenderBoard(), calculatedMoveField);

            for (Map.Entry<String, MoveField> entry : bestMoves.entrySet()) {
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
        long startTime = System.nanoTime();
        double max = -3333;
        double finalMax = max;
        Map<MoveField, Double> moveMap =
                moves.stream()
                        .collect(Collectors.toMap(m -> m, m -> getMinScoreForPredictingNextMovesAfterMove(board, m, color, finalMax, level)));

        max = moveMap.values().stream()
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("No max value found."));

        Set<MoveField> bestMoveFields = getKeysByValue(moveMap, max);


        /*for (MoveField move : moves) {
            min = getMinScoreForPredictingNextMovesAfterMove(board, move, color, max, level);
            if (min > max) {
                log.info("max was " + max + " now is " + min);
                max = min;
                bestMove = move;
            }
        }*/
        Random r = new Random();
        long stopTime = System.nanoTime();
        log.info(String.format("### All moves took [%s Seconds] ###", TimeUnit.NANOSECONDS.toSeconds((stopTime-startTime))));

        return bestMoveFields.stream().skip(r.nextInt(bestMoveFields.size())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No best move found."));
    }

    private double getMaxScoreOfAllPossibleMoves(Board board, List<MoveField> moves, String color, double min, double max, int level) {
        double maximum = max;

        double finalMaximum = maximum;
        long startTime = System.nanoTime();
        maximum = moves.stream()
                .map(m -> getMinScoreForPredictingNextMovesAfterMove(board, m, color, finalMaximum, level))
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("No max value found."));
        long stopTime = System.nanoTime();
        log.info(String.format("### All Moves: took [%ss] ###", TimeUnit.NANOSECONDS.toSeconds((stopTime-startTime))));
        return maximum;
    }

    private double getMinScoreForPredictingNextMovesAfterMove(Board board, MoveField move, String color, double max, int level) {
        log.info("-------------------------------------------------------------");
        long startTime = System.nanoTime();
        log.info("Move is " + move.toString());
        Board boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);

        double minScore = 3333;

        double scoreAfterFirstMove = boardAfterMove.getScore().getScoreDifference(color);
        if(engine.isInStateCheckMate(boardAfterMove, Color.getOpponentColor(color))) {
            scoreAfterFirstMove += boardAfterMove.getKings().get(0).getPoints();
        }

        log.trace(String.format("Score after First Move [%s]", scoreAfterFirstMove));

        if (scoreAfterFirstMove >= max) {
            log.trace(String.format("Score after First Move was >= max [%s]", scoreAfterFirstMove));

            List<MoveField> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterMove, Color.getOpponentColor(color));

            double finalMinScore = minScore;
            minScore = opponentMoves.stream()
                    .map(m -> calculateOpponent(color, max, level, boardAfterMove, finalMinScore, m))
                    .min(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("No min value found."));

            if (level <= 1) {
                log.info("minscore: " + minScore + " maxscore: " + max + " " + move.toString());
                long stopTime = System.nanoTime();
                log.info(String.format("### Move: [%s] took [%sms] ###", move.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime-startTime))));
                return minScore;
            }
        }
        log.trace("Returns scoreAfterFirstMove: " + scoreAfterFirstMove + " " + move.toString());
        long stopTime = System.nanoTime();

        log.info(String.format("### Move: [%s] took [%sms] ###", move.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime-startTime))));
        return scoreAfterFirstMove;
    }

    private double calculateOpponent(String color, double max, int level, Board boardAfterMove, double minScore, MoveField opponentMove) {
        long startTime = System.nanoTime();

        Board boardAfterSecondMove = engine.simulateMoveAndGetDummyBoard(boardAfterMove, opponentMove);

        double scoreAfterSecondMove = boardAfterSecondMove.getScore().getScoreDifference(color);
        if(engine.isInStateCheckMate(boardAfterSecondMove, color)) {
            scoreAfterSecondMove -= boardAfterSecondMove.getKings().get(0).getPoints();
        }
        log.trace(String.format("Score after Second Move: [%s]", scoreAfterSecondMove));

        if (scoreAfterSecondMove < minScore) {
            log.trace("MinScore is now: " + scoreAfterSecondMove);
            minScore = scoreAfterSecondMove;
            if (level > 1) {
                List<MoveField> nMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterSecondMove, color);

                log.trace("minscore: " + minScore + "going into recursion");
                minScore = getMaxScoreOfAllPossibleMoves(boardAfterSecondMove, nMoves, color, minScore, max, level - 1);
            }
            //boardAfterSecondMove.logBoard();
        }
        long stopTime = System.nanoTime();
        log.info(String.format("Opponent Move: [%s] took [%sms]", opponentMove.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime-startTime))));

        return minScore;
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
