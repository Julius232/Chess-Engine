package julius.game.chessengine.ai;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;

@Log4j2
@Component
public class AI {

    private final double MIN_SCORE = Double.NEGATIVE_INFINITY;
    private final double CHECKMATE_SCORE = 10000; // or some other large positive value

    private final Engine engine;

    public AI(Engine engine) {
        this.engine = engine;
    }

    public GameState executeCalculatedMove(String colorString) {
        // Convert the string color to the Color enum
        Color color = Color.valueOf(colorString.toUpperCase());

        // Calculate the move using the AI logic
        Move calculatedMove = calculateMove(engine.getBitBoard(), color);

        // Convert the Move to fromPosition and toPosition
        Position fromPosition = calculatedMove.getFrom();
        Position toPosition = calculatedMove.getTo();

        // Use the engine to move the figure on the bitboard
        return engine.moveFigure(engine.getBitBoard(), fromPosition, toPosition);
    }


    private Move calculateMove(BitBoard board, Color color) {
        int levelOfDepth = 7; // Adjust the level of depth according to your requirements

        // Get all possible moves for the given color
        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(color);

        // Sort moves by efficiency if needed
        // Assume sortMovesByEfficiency returns a sorted list of Move objects
        // This is just a placeholder; you'll need to implement the actual logic for sorting moves by efficiency
        LinkedList<Move> sortedMoves = sortMovesByEfficiency(moves, board, color);

        // Get the best move from the sorted list
        // This is just a placeholder; you'll need to implement the actual logic for selecting the best move
        long startTime = System.nanoTime(); // Start timing
        Move calculatedMove = getBestMove(board, sortedMoves, color, levelOfDepth);
        long endTime = System.nanoTime();

        log.info("Time taken for move calculation: {} ms", (endTime - startTime) / 1e6);

        // Log the calculated move
        log.info("Calculated Move is From: " + calculatedMove.getFrom()
                + " To: " + calculatedMove.getTo());

        // Return the calculated move directly without saving it
        return calculatedMove;
    }


    private LinkedList<Move> sortMovesByEfficiency(List<Move> moves, BitBoard board, Color color) {
        // We use a TreeMap to sort by the move efficiency value automatically.
        Map<Move, Integer> unsortedMoveMap = new HashMap<>();

        for (Move move : moves) {
            BitBoard dummy = new BitBoard(board); // Clone the board or create a new dummy board with the same state
            dummy.performMove(move); // Perform the move on the dummy board

            int efficiency;
            if (engine.isInStateCheckMate(dummy, Color.getOpponentColor(color))) {
                efficiency = 10000; // Assign a high value for checkmate
            } else if (dummy.isInCheck(Color.getOpponentColor(color))) {
                efficiency = 1000; // Assign a medium value for check
            } else {
                efficiency = 100; // Assign a base value for moves that do not result in check
            }
            unsortedMoveMap.put(move, efficiency);
        }

        // Sort the moves by their efficiency in descending order
        List<Map.Entry<Move, Integer>> sortedEntries = new ArrayList<>(unsortedMoveMap.entrySet());
        sortedEntries.sort(Map.Entry.<Move, Integer>comparingByValue().reversed());

        // Create a linked list to store the sorted moves
        LinkedList<Move> sortedMoves = new LinkedList<>();
        for (Map.Entry<Move, Integer> entry : sortedEntries) {
            sortedMoves.add(entry.getKey());
        }

        return sortedMoves;
    }

    private Move getBestMove(BitBoard board, LinkedList<Move> moves, Color color, int levelOfDepth) {
        Move bestMove = null;
        double bestScore = MIN_SCORE;
        double alpha = MIN_SCORE;
        double beta = Double.POSITIVE_INFINITY;

        for (Move move : moves) {
            double value = getMinScore(board, move, color, levelOfDepth - 1, alpha, beta);
            if (value > bestScore) {
                bestScore = value;
                bestMove = move;
                alpha = value;
            }
        }
        log.info("Best move score: " + bestScore);
        return bestMove;
    }

    private double getMinScore(BitBoard board, Move move, Color color, int depth, double alpha, double beta) {
        BitBoard boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
        if (engine.isInStateCheckMate(boardAfterMove, Color.getOpponentColor(color))) {
            return CHECKMATE_SCORE; // Positive value for checkmate in favor
        }
        if (depth == 0) {
            log.info("Board score at depth 0 for {}: {} - Move[{}]", color, board.getScore().getScoreDifference(color), move);
            return boardAfterMove.getScore().getScoreDifference(color);
        }
        return getMaxScore(boardAfterMove, Color.getOpponentColor(color), depth - 1, alpha, beta);
    }

    private double getMaxScore(BitBoard board, Color color, int depth, double alpha, double beta) {
        if (engine.isInStateCheckMate(board, color)) {
            // negative for opponent's checkmate
            return -CHECKMATE_SCORE; // Negative value for opponent's checkmate
        }
        if (depth == 0) {
            return board.getScore().getScoreDifference(color);
        }

        double maxScore = MIN_SCORE;
        List<Move> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(board, Color.getOpponentColor(color));
        for (Move move : opponentMoves) {
            BitBoard boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
            // Here we pass the 'move' to getMinScore as required by its parameters
            double score = getMinScore(boardAfterMove, move, Color.getOpponentColor(color), depth - 1, alpha, beta);
            maxScore = Math.max(maxScore, score);
            alpha = Math.max(alpha, score);
            if (beta <= alpha) {
                log.info("Pruning in getMaxScore at depth " + depth + " with score " + score);
                break;
            }
        }
        return maxScore;
    }

}
