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

    private static final Map<Long, TranspositionTableEntry> transpositionTable = new HashMap<>();

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
        int levelOfDepth = 5; // Adjust the level of depth according to your requirements

        // Get all possible moves for the given color
        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(color);

        // Get the best move from the sorted list
        // This is just a placeholder; you'll need to implement the actual logic for selecting the best move
        long startTime = System.nanoTime(); // Start timing
        Move calculatedMove = getBestMove(board, moves, color, levelOfDepth);
        long endTime = System.nanoTime();

        log.info("Time taken for move calculation: {} ms", (endTime - startTime) / 1e6);


        // Return the calculated move directly without saving it
        return calculatedMove;
    }

    private Move getBestMove(BitBoard board, List<Move> moves, Color color, int levelOfDepth) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        Move bestMove = null;
        double bestScore = color == Color.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        log.debug("Starting best move calculation for color {}", color);
        BitBoard dummyBoard = new BitBoard(board);
        for (Move move : sortMovesByEfficiency(moves, dummyBoard, color)) {
            dummyBoard.performMove(move);
            log.debug("Evaluating move: {}", move);
            double score = alphaBeta(dummyBoard, levelOfDepth - 1, alpha, beta, color == Color.WHITE, color);
            log.debug("Move {} evaluated with score {}", move, score);
            dummyBoard.undoMove(move);

            if (color == Color.WHITE && score > bestScore || color == Color.BLACK && score < bestScore) {
                bestScore = score;
                bestMove = move;
                log.info("New best move found: {} with score {}", move, bestScore);
            }
        }

        log.info("Score [{}] was best move [{}] calculation completed for color {}", bestScore, bestMove, color);
        return bestMove;
    }

    private double alphaBeta(BitBoard board, int depth, double alpha, double beta, boolean maximizingPlayer, Color color) {
        long boardHash = board.getBoardStateHash();
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

        if (depth == 0 || engine.isInStateCheckMate(board, color)) {
            double staticEval = board.getScore().getScoreDifference(color) * (maximizingPlayer ? 1 : -1);
            transpositionTable.put(boardHash, new TranspositionTableEntry(staticEval, depth, NodeType.EXACT));
            return staticEval;
        }

        double alphaOriginal = alpha;
        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(color);

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move move : sortMovesByEfficiency(moves, board, color)) {
                board.performMove(move);
                double eval = alphaBeta(board, depth - 1, alpha, beta, false, Color.getOpponentColor(color));
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            if (maxEval <= alphaOriginal) {
                transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.UPPERBOUND));
            } else if (maxEval >= beta) {
                transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.LOWERBOUND));
            } else {
                transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.EXACT));
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Move move : sortMovesByEfficiency(moves, board, color)) {
                board.performMove(move);
                double eval = alphaBeta(board, depth - 1, alpha, beta, true, Color.getOpponentColor(color));
                board.undoMove(move);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (alpha >= beta) {
                    break;
                }
            }
            if (minEval <= alphaOriginal) {
                transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.UPPERBOUND));
            } else if (minEval >= beta) {
                transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.LOWERBOUND));
            } else {
                transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.EXACT));
            }
            return minEval;
        }
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
                score += 5000; // Checks should have a high score
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
        List<Move> opponentMoves = board.getAllCurrentPossibleMoves();

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
}
