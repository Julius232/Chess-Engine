package julius.game.chessengine.ai;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
import julius.game.chessengine.figures.*;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;

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
        int levelOfDepth = 5;
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
            LinkedList<MoveField> sortedMoves = sortMovesByEfficiency(moves, board, color);

            //MoveField calculatedMoveField = getMaxScoreMoveOfAllPossibleMoves(board, moves, color, levelOfDepth);
            MoveField calculatedMoveField = getBestMove(board, sortedMoves, color, levelOfDepth);


            log.info("Calculated Move is From: " + calculatedMoveField.fromPositionToString()
                    + " To: " + calculatedMoveField.toPositionToString());
            //bestMoves.put(FEN.translateBoardToFEN(board).getRenderBoard(), calculatedMoveField);

            properties.store(new FileOutputStream(filePath), null);

            return calculatedMoveField;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;

    }

    private LinkedList<MoveField> sortMovesByEfficiency(List<MoveField> moves, Board board, String color) {
        LinkedHashMap<MoveField, Integer> sortedMoveMap = new LinkedHashMap<>();
        for(MoveField move : moves) {
            Board dummy = engine.simulateMoveAndGetDummyBoard(board, move);

            if(engine.isInStateCheckMate(dummy, Color.getOpponentColor(color))) {
                sortedMoveMap.put(move, 10000);
            }
            else if(dummy.isPlayerInStateCheck(Color.getOpponentColor(color))) {
                sortedMoveMap.put(move, 1000);
            }

            else {
                Figure figureFrom = board.getFigureForPosition(move.getFromPosition());
                try {
                    Figure figureTo = board.getFigureForPosition(move.getToPosition());
                    if (figureFrom instanceof Pawn) {
                        if (figureTo instanceof Queen) {
                            sortedMoveMap.put(move, 900);
                        }
                        if (figureTo instanceof Rook) {
                            sortedMoveMap.put(move, 500);
                        }
                        if (figureTo instanceof Knight || figureTo instanceof Bishop) {
                            sortedMoveMap.put(move, 300);
                        }
                        if (figureTo instanceof Pawn) {
                            sortedMoveMap.put(move, 100);
                        }
                    }
                    if (figureFrom instanceof Knight || figureFrom instanceof Bishop) {
                        if (figureTo instanceof Queen) {
                            sortedMoveMap.put(move, 810);
                        }
                        if (figureTo instanceof Rook) {
                            sortedMoveMap.put(move, 450);
                        }
                        if (figureTo instanceof Knight || figureTo instanceof Bishop) {
                            sortedMoveMap.put(move, 270);
                        }
                        if (figureTo instanceof Pawn) {
                            sortedMoveMap.put(move, 10);
                        }
                    }
                    if (figureFrom instanceof Rook) {
                        if (figureTo instanceof Queen) {
                            sortedMoveMap.put(move, 450);
                        }
                        if (figureTo instanceof Rook) {
                            sortedMoveMap.put(move, 250);
                        }
                        if (figureTo instanceof Knight || figureTo instanceof Bishop) {
                            sortedMoveMap.put(move, 30);
                        }
                        if (figureTo instanceof Pawn) {
                            sortedMoveMap.put(move, 3);
                        }
                    }
                    if (figureFrom instanceof Queen) {
                        if (figureTo instanceof Queen) {
                            sortedMoveMap.put(move, 100);
                        }
                        if (figureTo instanceof Rook) {
                            sortedMoveMap.put(move, 250);
                        }
                        if (figureTo instanceof Knight || figureTo instanceof Bishop) {
                            sortedMoveMap.put(move, 30);
                        }
                        if (figureTo instanceof Pawn) {
                            sortedMoveMap.put(move, 3);
                        }
                    }

                } catch (RuntimeException e) {
                    sortedMoveMap.put(move, 0);
                }
            }
        }
        orderByValue(sortedMoveMap, Comparator.reverseOrder());
        return new LinkedList<>(sortedMoveMap.keySet());
    }

    static <K, V> void orderByValue(
            LinkedHashMap<K, V> m, Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
        m.clear();
        entries.stream()
                .sorted(Map.Entry.comparingByValue(c))
                .forEachOrdered(e -> m.put(e.getKey(), e.getValue()));
    }

    private MoveField getBestMove(Board board, LinkedList<MoveField> moves, String color, int levelOfDepth) {
        MoveField bestMove = null;
        double best = -9999999;

        double alpha = -9999999;
        double beta = 9999999;

        for (MoveField move : moves) {
            double value = getMinScore(board, move, color, levelOfDepth - 1, alpha, beta);
            if(value > 1000) {
                return move;
            }
            if (value > best) {
                best = value;
                bestMove = move;
            }
            if (value > alpha) {
                alpha = value;
            }
            if (beta <= alpha) {
                log.info("I think this should never happen.");
                break;
            }
        }
        log.info("best move score: " + best);
        return bestMove;
    }

    private double getMinScore(Board board, MoveField move, String color, int depth, double alpha, double beta) {

        Board boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
        double score = boardAfterMove.getScore().getScoreDifference(color);
        if (engine.isInStateCheckMate(boardAfterMove, Color.getOpponentColor(color))) {
            score += boardAfterMove.getKings().get(0).getPoints();
            log.info(String.format("Depth[%s]: CHECKMATE FOUND! score[%s] alpha[%s] beta[%s]", depth, score, alpha, beta));
            boardAfterMove.logBoard();
            return score;
        }
        if (depth == 0) {
            return score;
        }
        return getMaxScore(boardAfterMove, color, depth - 1, alpha, beta);
    }

    private double getMaxScore(Board boardAfterMove, String color, int depth, double alpha, double beta) {
        List<MoveField> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterMove, Color.getOpponentColor(color));
        double max = 9999999;
        double score;
        for (MoveField move : opponentMoves) {
            Board boardAfterSecondMove = engine.simulateMoveAndGetDummyBoard(boardAfterMove, move);
            score = boardAfterSecondMove.getScore().getScoreDifference(color);
            if (engine.isInStateCheckMate(boardAfterSecondMove, color)) {
                score -= boardAfterSecondMove.getKings().get(0).getPoints();
                log.info(String.format("Depth[%s]: CHECKMATE FOUND! score[%s] alpha[%s] beta[%s]", depth, score, alpha, beta));
                boardAfterSecondMove.logBoard();
            }
            if (depth > 0) {
                max = getMinScore(boardAfterSecondMove, color, depth - 1, alpha, beta);
            }
            else if (score < max) {
                max = score;
            }
            if (beta > score) {
                beta = score;
            }
            if (beta <= alpha) {
                log.info(depth + ") Pruning getMaxScore, score was: " + score + " move was: " + move.toString());
                break;
            }
        }
        return max;
    }

    private double getMinScore(Board boardAfterSecondMove, String color, int depth, double alpha, double beta) {
        List<MoveField> moves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterSecondMove, color);
        double min = -9999999;
        double score;
        for (MoveField move : moves) {
            Board boardAfterThirdMove = engine.simulateMoveAndGetDummyBoard(boardAfterSecondMove, move);
            score = boardAfterThirdMove.getScore().getScoreDifference(color);
            if (engine.isInStateCheckMate(boardAfterThirdMove, Color.getOpponentColor(color))) {
                score += boardAfterThirdMove.getKings().get(0).getPoints();
                log.info(String.format("Depth[%s]: CHECKMATE FOUND! score[%s] alpha[%s] beta[%s]", depth, score, alpha, beta));
                boardAfterThirdMove.logBoard();
            }
            if (depth > 0) {
                min = getMaxScore(boardAfterThirdMove, color, depth - 1, alpha, beta);
            }
            else if (score > min) {
                min = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (beta <= alpha) {
                log.info(depth + ") Pruning getMinscore, score was: " + score + " move was: " + move.toString());
                break;
            }
        }
        return min;
    }


    /*private MoveField getMaxScoreMoveOfAllPossibleMoves(Board board, List<MoveField> moves, String color, int level) {
        long startTime = System.nanoTime();
        double max = -3333;
        double finalMax = max;
        Map<MoveField, Double> moveMap =
                moves.stream()
                        .collect(Collectors.toMap(m -> m, m -> getMinScoreForPredictingNextMovesAfterMove(board, m, color, finalMax, level)));

        max = moveMap.values().stream()
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("No max value found."));

        Set<MoveField> bestMoveFields = getKeysByValue(moveMap, max);


        *//*for (MoveField move : moves) {
            min = getMinScoreForPredictingNextMovesAfterMove(board, move, color, max, level);
            if (min > max) {
                log.info("max was " + max + " now is " + min);
                max = min;
                bestMove = move;
            }
        }*//*
        Random r = new Random();
        long stopTime = System.nanoTime();
        log.info(String.format("### All moves took [%s Seconds] ###", TimeUnit.NANOSECONDS.toSeconds((stopTime-startTime))));

        return bestMoveFields.stream().skip(r.nextInt(bestMoveFields.size())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No best move found."));
    }*/

    private double getMaxScoreOfAllPossibleMoves(Board board, List<MoveField> moves, String color, double min, double max, int level) {
        double maximum = max;

        double finalMaximum = maximum;
        long startTime = System.nanoTime();
        maximum = moves.stream()
                .map(m -> getMinScoreForPredictingNextMovesAfterMove(board, m, color, finalMaximum, level))
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("No max value found."));
        long stopTime = System.nanoTime();
        log.info(String.format("### All Moves: took [%ss] ###", TimeUnit.NANOSECONDS.toSeconds((stopTime - startTime))));
        return maximum;
    }

    private double getMinScoreForPredictingNextMovesAfterMove(Board board, MoveField move, String color, double max, int level) {
        log.info("-------------------------------------------------------------");
        long startTime = System.nanoTime();
        log.info("Move is " + move.toString());
        Board boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);

        double minScore = 3333;

        double scoreAfterFirstMove = boardAfterMove.getScore().getScoreDifference(color);
        if (engine.isInStateCheckMate(boardAfterMove, Color.getOpponentColor(color))) {
            scoreAfterFirstMove += boardAfterMove.getKings().get(0).getPoints();
        }

        log.trace(String.format("Score after First Move [%s]", scoreAfterFirstMove));

        if (scoreAfterFirstMove >= max) {
            log.trace(String.format("Score after First Move was >= max [%s]", scoreAfterFirstMove));

            List<MoveField> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(boardAfterMove, Color.getOpponentColor(color));

            double finalMinScore = minScore;
            minScore = opponentMoves.stream()
                    .map(m -> calculateOpponent(color, max, level, boardAfterMove, finalMinScore, m))
                    .min(Comparator.naturalOrder()).orElse(MAX_VALUE);

            if (level <= 1) {
                log.info("minscore: " + minScore + " maxscore: " + max + " " + move.toString());
                long stopTime = System.nanoTime();
                log.info(String.format("### Move: [%s] took [%sms] ###", move.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime - startTime))));
                return minScore;
            }
        }
        log.trace("Returns scoreAfterFirstMove: " + scoreAfterFirstMove + " " + move.toString());
        long stopTime = System.nanoTime();

        log.info(String.format("### Move: [%s] took [%sms] ###", move.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime - startTime))));
        return scoreAfterFirstMove;
    }

    private double calculateOpponent(String color, double max, int level, Board boardAfterMove, double minScore, MoveField opponentMove) {
        long startTime = System.nanoTime();

        Board boardAfterSecondMove = engine.simulateMoveAndGetDummyBoard(boardAfterMove, opponentMove);

        double scoreAfterSecondMove = boardAfterSecondMove.getScore().getScoreDifference(color);
        if (engine.isInStateCheckMate(boardAfterSecondMove, color)) {
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
        log.info(String.format("Opponent Move: [%s] took [%sms]", opponentMove.toString(), TimeUnit.NANOSECONDS.toMillis((stopTime - startTime))));

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
