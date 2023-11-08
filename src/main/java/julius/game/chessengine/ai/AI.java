package julius.game.chessengine.ai;

import julius.game.chessengine.board.BitBoard;
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
        Map<Move, Integer> moveEfficiencyMap = new HashMap<>();

        for (Move move : moves) {
            BitBoard dummy = new BitBoard(board); // Clone the board or create a new dummy board with the same state
            dummy.performMove(move); // Perform the move on the dummy board

            // Start with a base score for sorting
            int score = 0;

            // Check for checkmate or check
            if (engine.isInStateCheckMate(dummy, Color.getOpponentColor(color))) {
                score += 10000; // Checkmate should have the highest score
            } else if (dummy.isInCheck(Color.getOpponentColor(color))) {
                score += 500; // Checks should have a high score
            }

            // Captures
            if (move.isCapture()) {
                // Add the value of the captured piece to the score
                score += getPieceValue(dummy.getPieceTypeAtPosition(move.getTo()));
            }

            // Add points for threats (attacking high-value pieces without capturing)
            score += evaluateThreats(move, dummy, color);

            // Use the score for sorting
            moveEfficiencyMap.put(move, score);
        }

        // Sort the moves by their efficiency in descending order
        List<Map.Entry<Move, Integer>> sortedEntries = new ArrayList<>(moveEfficiencyMap.entrySet());
        sortedEntries.sort(Map.Entry.<Move, Integer>comparingByValue().reversed());

        // Create a linked list to store the sorted moves
        LinkedList<Move> sortedMoves = new LinkedList<>();
        for (Map.Entry<Move, Integer> entry : sortedEntries) {
            sortedMoves.add(entry.getKey());
        }

        return sortedMoves;
    }

    private int getPieceValue(PieceType pieceType) {
        return switch (pieceType) {
            case PAWN -> 100;
            case KNIGHT, BISHOP -> 300;
            case ROOK -> 500;
            case QUEEN -> 900;
            default -> 0; // King has no value because it cannot be captured
        };
    }

    private int evaluateThreats(Move move, BitBoard board, Color color) {
        int threatScore = 0;

        // Simulate the move on the board
        board.performMove(move);

        // Get all possible moves for the opponent after this move
        List<Move> opponentMoves = board.generateAllPossibleMoves(Color.getOpponentColor(color));

        // Analyze each of the opponent's moves
        for (Move opponentMove : opponentMoves) {
            // Check if the move is a capture move
            if (opponentMove.isCapture()) {
                // Get the piece at the destination
                PieceType capturedPiece = board.getPieceTypeAtPosition(opponentMove.getTo());

                // Check if the piece at the destination is the same as the one moved by the player
                if (capturedPiece == move.getPieceType()) {
                    // If so, then the player's move has created a threat to that piece
                    // You can adjust the scores based on the importance of the piece threatened
                    // For simplicity, using the getPieceValue method to get the value of the threatened piece
                    threatScore += getPieceValue(capturedPiece);
                }
            }
        }

        // Undo the move to restore the board state
        board.undoMove(move);

        return threatScore;
    }

    private Move getBestMove(BitBoard board, LinkedList<Move> moves, Color color, int levelOfDepth) {
        Move bestMove = null;
        double MIN_SCORE = Double.NEGATIVE_INFINITY;
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
            log.info("Checkmate detected for {}, score: {}", Color.getOpponentColor(color), CHECKMATE_SCORE);
            return CHECKMATE_SCORE;
        }
        if (depth == 0) {
            double score = boardAfterMove.getScore().getScoreDifference(color);
            log.info("Depth 0 for {}, move: {}, score: {}", color, move, score);
            return score;
        }

        double minScore = Double.POSITIVE_INFINITY;
        List<Move> opponentMoves = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);
        // Sort opponent moves before examining them
        LinkedList<Move> sortedOpponentMoves = sortMovesByEfficiency(opponentMoves, board, Color.getOpponentColor(color));
        for (Move opponentMove : sortedOpponentMoves) {
            BitBoard boardAfterOpponentMove = engine.simulateMoveAndGetDummyBoard(board, opponentMove);
            double score = getMaxScore(boardAfterOpponentMove, Color.getOpponentColor(color), depth - 1, alpha, beta);
            if (score < minScore) {
                minScore = score;
                beta = Math.min(beta, score);
                log.info("Better min score found at depth {}: move: {}, score: {}", depth, opponentMove, score);
            }
            if (beta <= alpha) {
                log.info("Pruning in getMinScore at depth {} with beta: {}, alpha: {}", depth, beta, alpha);
                break; // Alpha cut-off
            }
        }
        return minScore;
    }

    private double getMaxScore(BitBoard board, Color color, int depth, double alpha, double beta) {
        if (engine.isInStateCheckMate(board, color)) {
            log.info("Checkmate detected for {}, score: {}", color, -CHECKMATE_SCORE);
            return -CHECKMATE_SCORE;
        }
        if (depth == 0) {
            double score = board.getScore().getScoreDifference(color);
            log.info("Depth 0 for {}, score: {}", color, score);
            return score;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        List<Move> moves = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);
        // Sort moves before examining them
        LinkedList<Move> sortedMoves = sortMovesByEfficiency(moves, board, color);
        for (Move move : sortedMoves) {
            BitBoard boardAfterMove = engine.simulateMoveAndGetDummyBoard(board, move);
            double score = getMinScore(boardAfterMove, move, Color.getOpponentColor(color), depth - 1, alpha, beta);
            if (score > maxScore) {
                maxScore = score;
                alpha = Math.max(alpha, score);
                log.info("Better max score found at depth {}: move: {}, score: {}", depth, move, score);
            }
            if (beta <= alpha) {
                log.info("Pruning in getMaxScore at depth {} with beta: {}, alpha: {}", depth, beta, alpha);
                break; // Beta cut-off
            }
        }
        return maxScore;
    }


}
