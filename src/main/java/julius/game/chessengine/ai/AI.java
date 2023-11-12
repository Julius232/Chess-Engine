package julius.game.chessengine.ai;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;

@Log4j2
@Component
public class AI {

    private static final Map<Long, TranspositionTableEntry> transpositionTable = new HashMap<>();
    private static final double TIME_LIMIT_EXCEEDED_FLAG = Double.MAX_VALUE;

    // Adjust the level of depth according to your requirements
    int maxDepth = 18;
    long timeLimit = 20000; // 10 seconds in milliseconds
    private final Engine engine;

    public AI(Engine engine) {
        this.engine = engine;
    }

    public void startAutoPlay() {
        while (engine.getGameState().getState().equals("PLAY")) {
            engine.logBoard();
            executeCalculatedMove();
        }
    }

    public GameState executeCalculatedMove() {
        // Convert the string color to the Color enum
        Color color = engine.whitesTurn() ? Color.WHITE : Color.BLACK;

        Move bestMove = null;
        double bestScore = color == Color.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        long startTime = System.currentTimeMillis(); // Record start time

        for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
            MoveAndScore moveAndScore = getBestMove(engine.createSimulation(), engine.getAllLegalMoves(), color, currentDepth, startTime, timeLimit);

            if (moveAndScore != null && (color == Color.WHITE ? moveAndScore.score > bestScore : moveAndScore.score < bestScore)) {
                bestScore = moveAndScore.score;
                bestMove = moveAndScore.move;
                log.info("New best move found: {}, depth: {}", bestMove, currentDepth);
            }

            if (System.currentTimeMillis() - startTime > timeLimit) {
                // Time limit exceeded, break out of the loop
                break;
            }
        }

        if (bestMove == null) {
            throw new IllegalStateException("No move found within the time limit");
        }

        // Execute the best move found within the time frame
        Position fromPosition = bestMove.getFrom();
        Position toPosition = bestMove.getTo();
        return engine.moveFigure(fromPosition, toPosition);
    }

    private MoveAndScore getBestMove(Engine engine, List<Move> moves, Color color, int levelOfDepth, long startTime, long timeLimit) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        Move bestMove = null;
        double bestScore = color == Color.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        Color opponentColor = Color.getOpponentColor(color);
        ArrayList<Move> sortedMoves = sortMovesByEfficiency(moves, engine, color);

        for (Move move : sortedMoves) {
            // Check if time limit exceeded
            if (System.currentTimeMillis() - startTime > timeLimit) {
                break;
            }

            engine.performMove(move);

            if (engine.isInStateCheckMate(opponentColor)) {
                engine.undoMove(move, false);
                return new MoveAndScore(move, color.equals(Color.WHITE) ? Engine.CHECKMATE : -Engine.CHECKMATE);
            }

            double score = alphaBeta(engine, levelOfDepth - 1, alpha, beta, Color.WHITE == opponentColor, opponentColor, startTime, timeLimit);
            engine.undoMove(move, false);

            // Check for time limit exceeded
            if (score == TIME_LIMIT_EXCEEDED_FLAG) {
                break;
            }

            if (color.equals(Color.WHITE) ? score == Engine.CHECKMATE : score == -Engine.CHECKMATE) {
                return new MoveAndScore(move, score);
            }

            if (Color.WHITE == color ? score > bestScore : score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove != null ? new MoveAndScore(bestMove, bestScore) : null;
    }


    /**
     * *
     * 5rkr/pp2Rp2/1b1p1Pb1/3P2Q1/2n3P1/2p5/P4P2/4R1K1 w - - 1 0
     * *
     */
    private double alphaBeta(Engine engine, int depth, double alpha, double beta, boolean maximizingPlayer, Color color, long startTime, long timeLimit) {
        // Check for time limit exceeded
        if (System.currentTimeMillis() - startTime > timeLimit) {
            return TIME_LIMIT_EXCEEDED_FLAG;
        }

        long boardHash = engine.getBoardStateHash();

        if (depth == 0 || engine.isGameOver()) {
            double eval = engine.evaluateBoard(color, maximizingPlayer);
            // Store the evaluation in the transposition table
            transpositionTable.put(boardHash, new TranspositionTableEntry(eval, depth, NodeType.EXACT));

            return eval;
        }

        TranspositionTableEntry entry = transpositionTable.get(boardHash);

        if (entry != null && entry.depth >= depth) {
            if (entry.nodeType == NodeType.EXACT) {
                return entry.score;
            }
            if (entry.nodeType == NodeType.LOWERBOUND && entry.score > alpha) {
                alpha = entry.score;
            } else if (entry.nodeType == NodeType.UPPERBOUND && entry.score < beta) {
                beta = entry.score;
            }
            if (alpha >= beta) {
                return entry.score;
            }
        }


        double alphaOriginal = alpha;
        List<Move> moves = engine.getAllLegalMoves();

        if (maximizingPlayer) {
            return maximizer(engine, depth, alpha, beta, color, boardHash, alphaOriginal, moves, startTime, timeLimit);
        } else {
            return minimizer(engine, depth, alpha, beta, color, boardHash, alphaOriginal, moves, startTime, timeLimit);
        }
    }

    private double maximizer(Engine engine, int depth, double alpha, double beta, Color color, long boardHash, double alphaOriginal, List<Move> moves, long startTime, long timeLimit) {
        double maxEval = Double.NEGATIVE_INFINITY;
        for (Move move : sortMovesByEfficiency(moves, engine, color)) {
            engine.performMove(move);
            double eval = alphaBeta(engine, depth - 1, alpha, beta, false, Color.getOpponentColor(color), startTime, timeLimit);
            engine.undoMove(move, false);

            if (eval == TIME_LIMIT_EXCEEDED_FLAG) { // Time limit exceeded
                return TIME_LIMIT_EXCEEDED_FLAG;
            }

            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            if (beta <= alpha) {
                break;
            }
        }
        updateTanspositionTable(depth, beta, boardHash, alphaOriginal, maxEval);
        return maxEval;
    }


    private double minimizer(Engine engine, int depth, double alpha, double beta, Color color, long boardHash, double alphaOriginal, List<Move> moves, long startTime, long timeLimit) {
        double minEval = Double.POSITIVE_INFINITY;
        for (Move move : sortMovesByEfficiency(moves, engine, color)) {
            engine.performMove(move);
            double eval = alphaBeta(engine, depth - 1, alpha, beta, true, Color.getOpponentColor(color), startTime, timeLimit);
            engine.undoMove(move, false);

            if (eval == TIME_LIMIT_EXCEEDED_FLAG) { // Time limit exceeded
                return TIME_LIMIT_EXCEEDED_FLAG;
            }

            minEval = Math.min(minEval, eval);
            beta = Math.min(beta, eval);
            if (alpha >= beta) {
                break;
            }
        }
        updateTanspositionTable(depth, beta, boardHash, alphaOriginal, minEval);
        return minEval;
    }


    private void updateTanspositionTable(int depth, double beta, long boardHash, double alphaOriginal, double maxEval) {
        if (maxEval <= alphaOriginal) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.UPPERBOUND));
        } else if (maxEval >= beta) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.LOWERBOUND));
        } else {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.EXACT));
        }
    }


    private ArrayList<Move> sortMovesByEfficiency(List<Move> moves, Engine engine, Color color) {
        // We use a TreeMap to sort by the move efficiency value automatically.
        Map<Move, Double> moveEfficiencyMap = new HashMap<>();

        for (Move move : moves) {
            // Clone the board or create a new dummy board with the same state
            engine.performMove(move); // Perform the move on the dummy board

            // Start with a base score for sorting
            double score = 0;


            if (transpositionTable.containsKey(engine.getBoardStateHash())) {
                score = color == Color.WHITE ?
                        transpositionTable.get(engine.getBoardStateHash()).score :
                        (transpositionTable.get(engine.getBoardStateHash()).score * -1);
            } else {
                score = engine.evaluateBoard(color, Color.WHITE.equals(color));
            }

            // Use the score for sorting
            moveEfficiencyMap.put(move, score);
            engine.undoMove(move, false);
        }

        // Sort the moves by their efficiency in descending order
        List<Map.Entry<Move, Double>> sortedEntries = new ArrayList<>(moveEfficiencyMap.entrySet());

        if(color == Color.WHITE) {
            sortedEntries.sort(Map.Entry.<Move, Double>comparingByValue().reversed());

        } else {
            sortedEntries.sort(Map.Entry.<Move, Double>comparingByValue());
        }

        // Create a linked list to store the sorted moves
        ArrayList<Move> sortedMoves = new ArrayList<>();
        for (Map.Entry<Move, Double> entry : sortedEntries) {
            sortedMoves.add(entry.getKey());
        }

        return sortedMoves;
    }


}
