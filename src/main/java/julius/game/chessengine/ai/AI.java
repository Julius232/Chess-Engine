package julius.game.chessengine.ai;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log4j2
@Component
public class AI implements ApplicationListener<ContextRefreshedEvent> {

    private List<Move> calculatedLine = Collections.synchronizedList(new ArrayList<>());

    private static final ConcurrentHashMap<Long, TranspositionTableEntry> transpositionTable = new ConcurrentHashMap<>();
    private static final double TIME_LIMIT_EXCEEDED_FLAG = Double.MAX_VALUE;

    private Thread calculationThread;
    private volatile boolean keepCalculating = true;

    private volatile long lastCalculatedHash = -1;

    // Adjust the level of depth according to your requirements
    int maxDepth = 18;
    long timeLimit = 30000; //milliseconds
    private final Engine engine;


    public AI(Engine engine) {
        this.engine = engine;
        startCalculationThread();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Start the thread when the application context is fully refreshed
        startCalculationThread();
    }

    private void startCalculationThread() {
        keepCalculating = true;
        calculationThread = new Thread(this::calculateLine);
        calculationThread.start();
    }

    public void stopCalculation() {
        keepCalculating = false;
        calculationThread.interrupt();
    }

    public void startAutoPlay() {
        while (engine.getGameState().getState().equals("PLAY")) {
            performMove();
        }
    }

    public GameState performMove() {
        engine.performMove(calculatedLine.get(0));
        return engine.getGameState();
    }

    public void logMoveLine(Engine engine) {
        long currentBoardHash = engine.getBoardStateHash();
        List<Move> moveLine = new ArrayList<>();
        int i = 0;
        while (transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove != null) {
            TranspositionTableEntry entry = transpositionTable.get(currentBoardHash);
            Move move = entry.bestMove;
            moveLine.add(i, move); // Add at the beginning to reverse the order
            engine.performMove(move);
            currentBoardHash = engine.getBoardStateHash();
            i++;
        }

        // Log the move line
        log.info("Move Line: {}", moveLine.stream().map(Move::toString).collect(Collectors.joining(", ")));

        // Redo the moves to restore the original board state
        for (Move move : moveLine) {
            engine.undoMove(move, true); // Undo the move to get the previous board hash
        }
    }


    private void calculateLine() {
        while (keepCalculating) {
            if (positionChanged()) {
                log.info(" --- TranspositionTable[{}] --- ", transpositionTable.size());
                // Convert the string color to the Color enum
                Color color = engine.whitesTurn() ? Color.WHITE : Color.BLACK;
                Engine simulation = engine.createSimulation();

                Move bestMove;
                double bestScore = color == Color.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                long startTime = System.currentTimeMillis(); // Record start time

                for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
                    if (!keepCalculating) break; // Check if we should stop calculating

                    MoveAndScore moveAndScore = getBestMove(simulation, color, currentDepth, startTime, timeLimit);
                    log.info(" --- DEPTH<{}> --- ", currentDepth);
                    if (moveAndScore != null) {
                        double score = moveAndScore.score;
                        Move move = moveAndScore.move;
                        if (move != null && (color == Color.WHITE ? score > bestScore : score < bestScore)) {
                            bestScore = moveAndScore.score;
                            bestMove = move;
                            transpositionTable.put(engine.getBoardStateHash(), new TranspositionTableEntry(moveAndScore.score, currentDepth, NodeType.EXACT, bestMove));
                        }
                        fillCalculatedLine(simulation);
                    }

                    if (System.currentTimeMillis() - startTime > timeLimit) {
                        log.info("Time limit exceeded at depth {}", currentDepth);
                        break;
                    }
                }

                lastCalculatedHash = engine.getBoardStateHash();
            }

            // Sleep logic to prevent over-utilization of CPU
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Calculation thread was interrupted", e);
                return;
            }
        }
    }

    private boolean positionChanged() {
        return engine.getBoardStateHash() != lastCalculatedHash;
    }

    private synchronized void fillCalculatedLine(Engine simulation) {
        long currentBoardHash = simulation.getBoardStateHash();
        List<Move> newCalculatedLine = new ArrayList<>();
        int i = 0;
        while (transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove != null) {
            TranspositionTableEntry entry = transpositionTable.get(currentBoardHash);
            Move move = entry.bestMove;
            newCalculatedLine.add(i, move); // Add at the beginning to reverse the order
            simulation.performMove(move);
            currentBoardHash = simulation.getBoardStateHash();
            i++;
        }

        // Log the move line
        log.info("Move Line: {}", newCalculatedLine.stream().map(Move::toString).collect(Collectors.joining(", ")));

        // Redo the moves to restore the original board state
        for (Move move : newCalculatedLine) {
            simulation.undoMove(move, true); // Undo the moves
        }
        this.calculatedLine = newCalculatedLine;
    }

    private MoveAndScore getBestMove(Engine engine, Color color, int levelOfDepth, long startTime, long timeLimit) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        Move bestMove = null;
        double bestScore = color == Color.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        Color opponentColor = Color.getOpponentColor(color);
        ArrayList<Move> sortedMoves = sortMovesByEfficiency(engine.getAllLegalMoves(), engine, color);

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
            return engine.evaluateBoard(color, maximizingPlayer);
        }

        TranspositionTableEntry entry = transpositionTable.get(boardHash);

        if (entry != null && entry.depth > depth) {
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

        double alphaOriginal = alpha; // Store the original alpha value
        double betaOriginal = beta;   // Store the original beta value

        List<Move> moves = engine.getAllLegalMoves();

        if (maximizingPlayer) {
            return maximizer(engine, depth, alpha, beta, color, boardHash, alphaOriginal, betaOriginal, moves, startTime, timeLimit);
        } else {
            return minimizer(engine, depth, alpha, beta, color, boardHash, alphaOriginal, betaOriginal, moves, startTime, timeLimit);
        }
    }


    private double maximizer(Engine engine, int depth, double alpha, double beta, Color color, long boardHash, double alphaOriginal, double betaOriginal, List<Move> moves, long startTime, long timeLimit) {
        double maxEval = Double.NEGATIVE_INFINITY;
        Move bestMoveAtThisNode = null; // Variable to track the best move at this node

        for (Move move : sortMovesByEfficiency(moves, engine, color)) {
            engine.performMove(move);
            long newBoardHash = engine.getBoardStateHash();

            double eval;
            TranspositionTableEntry entry = transpositionTable.get(newBoardHash);

            if (entry != null && entry.depth >= depth) {
                eval = entry.score; // Use the score from the transposition table
            } else {
                eval = alphaBeta(engine, depth - 1, alpha, beta, false, Color.getOpponentColor(color), startTime, timeLimit);

                if (eval == TIME_LIMIT_EXCEEDED_FLAG) {
                    // If time limit exceeded, exit the loop
                    engine.undoMove(move, false);
                    return TIME_LIMIT_EXCEEDED_FLAG;
                }
            }

            engine.undoMove(move, false);

            if (eval > maxEval) { // Found a better evaluation
                maxEval = eval;
                bestMoveAtThisNode = move; // Update the best move
            }

            alpha = Math.max(alpha, eval);
            if (beta <= alpha) {
                break; // Alpha-beta pruning
            }
        }

        // After the for loop, update the transposition table with the best move
        if (maxEval <= alphaOriginal) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.UPPERBOUND, bestMoveAtThisNode));
        } else if (maxEval >= beta) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.LOWERBOUND, bestMoveAtThisNode));
        } else {
            transpositionTable.put(boardHash, new TranspositionTableEntry(maxEval, depth, NodeType.EXACT, bestMoveAtThisNode));
        }

        return maxEval;
    }


    private double minimizer(Engine engine, int depth, double alpha, double beta,
                             Color color, long boardHash, double alphaOriginal,
                             double betaOriginal, List<Move> moves, long startTime,
                             long timeLimit) {
        double minEval = Double.POSITIVE_INFINITY;
        Move bestMoveAtThisNode = null; // Track the best move at this node

        for (Move move : sortMovesByEfficiency(moves, engine, color)) {
            engine.performMove(move);
            long newBoardHash = engine.getBoardStateHash();

            double eval;
            TranspositionTableEntry entry = transpositionTable.get(newBoardHash);

            if (entry != null && entry.depth >= depth) {
                eval = entry.score;
            } else {
                eval = alphaBeta(engine, depth - 1, alpha, beta, true, Color.getOpponentColor(color), startTime, timeLimit);

                if (eval == TIME_LIMIT_EXCEEDED_FLAG) {
                    engine.undoMove(move, false);
                    return TIME_LIMIT_EXCEEDED_FLAG;
                }
            }

            engine.undoMove(move, false);

            if (eval < minEval) {
                minEval = eval;
                bestMoveAtThisNode = move; // Update the best move at this node
            }

            beta = Math.min(beta, eval);
            if (alpha >= beta) {
                break;
            }
        }

        if (minEval <= alpha) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.LOWERBOUND, bestMoveAtThisNode));
        } else if (minEval >= betaOriginal) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.UPPERBOUND, bestMoveAtThisNode));
        } else {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.EXACT, bestMoveAtThisNode));
        }

        return minEval;
    }


    private ArrayList<Move> sortMovesByEfficiency(List<Move> moves, Engine engine, Color color) {
        // We use a TreeMap to sort by the move efficiency value automatically.
        Map<Move, Double> moveEfficiencyMap = new HashMap<>();

        for (Move move : moves) {
            // Clone the board or create a new dummy board with the same state
            engine.performMove(move);

            // Start with a base score for sorting
            double score;


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

        if (color == Color.WHITE) {
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

    public List<Move> getCalculatedLine() {
        return this.calculatedLine;
    }

}
