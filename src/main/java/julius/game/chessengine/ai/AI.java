package julius.game.chessengine.ai;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import lombok.Getter;
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

    @Getter
    private List<Move> calculatedLine = Collections.synchronizedList(new ArrayList<>());

    private static final ConcurrentHashMap<Long, TranspositionTableEntry> transpositionTable = new ConcurrentHashMap<>();
    private static final double EXIT_FLAG = Double.MAX_VALUE;

    private Thread calculationThread;
    private volatile boolean keepCalculating = true;

    private volatile long lastCalculatedHash = -1;

    // Adjust the level of depth according to your requirements
    int maxDepth = 18;
    long timeLimit = 5000; //milliseconds
    private final Engine engine;


    public AI(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("startedUp");
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

    public void startAutoPlay() throws InterruptedException {
        while (engine.getGameState().getState().equals("PLAY")) {
            Thread.sleep(timeLimit);
            performMove();
        }
    }

    public GameState performMove() {
        if (calculatedLine.isEmpty()) {
            // calculatedLine is empty, either log an error or return null
            log.info("Waiting for calculatedLine to be populated");
            return null; // or handle differently
        }

        Move aiMove = calculatedLine.remove(0); // Retrieve and remove the move from the list
        if (aiMove == null) {
            log.error("Calculated move is null");
            return null; // or handle differently
        }

        engine.performMove(aiMove);
        return engine.getGameState();
    }

    private void calculateLine() {
        while (keepCalculating) {
            if (positionChanged()) {
                log.info(" --- TranspositionTable[{}] --- ", transpositionTable.size());
                // Convert the string color to the Color enum
                Engine simulation = engine.createSimulation();
                long boardStateHash = simulation.getBoardStateHash();
                lastCalculatedHash = boardStateHash;
                boolean isWhite = simulation.whitesTurn();

                Move bestMove;
                double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                long startTime = System.currentTimeMillis(); // Record start time

                for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
                    if (!keepCalculating) break; // Check if we should stop calculating
                    MoveAndScore moveAndScore = getBestMove(simulation, isWhite, currentDepth, startTime, timeLimit);
                    log.info(" --- DEPTH<{}> --- ", currentDepth);
                    if (moveAndScore != null) {
                        double score = moveAndScore.score;
                        Move move = moveAndScore.move;
                        if (move != null && (isWhite ? score > bestScore : score < bestScore)) {
                            bestScore = moveAndScore.score;
                            bestMove = move;
                            transpositionTable.put(boardStateHash, new TranspositionTableEntry(moveAndScore.score, currentDepth, NodeType.EXACT, bestMove));
                        }
                        fillCalculatedLine(simulation);
                    }

                    if (System.currentTimeMillis() - startTime > timeLimit) {
                        log.info("Time limit exceeded at depth {}", currentDepth);
                        break;
                    }
                }

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

    private void fillCalculatedLine(Engine simulation) {
        long currentBoardHash = simulation.getBoardStateHash();
        List<Move> newCalculatedLine = new LinkedList<>();

        if(!transpositionTable.containsKey(currentBoardHash)) {
            log.info("[{}] hash not exists", currentBoardHash);
        }

        if(transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove == null) {
            log.info("[{}] hash exists but move: " + transpositionTable.get(currentBoardHash), currentBoardHash);
        }

        while (transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove != null) {
            log.info("[{}] hash exists and move: {}", currentBoardHash, transpositionTable.get(currentBoardHash));
            TranspositionTableEntry entry = transpositionTable.get(currentBoardHash);
            Move move = entry.bestMove;
            newCalculatedLine.add(0, move); // Add at the beginning to maintain the order
            simulation.performMove(move);
            currentBoardHash = simulation.getBoardStateHash();
        }

        try {
            for (int i = 0; i < newCalculatedLine.size(); i++) {
                simulation.undoLastMove(); // Undo the moves in reverse order
            }
        } catch (Exception e) {
            log.error("Error while undoing moves", e);
            // Handle exception or rethrow
        }

        Collections.reverse(newCalculatedLine);
        this.calculatedLine = new ArrayList<>(newCalculatedLine);

        log.debug("Move Line: {}", newCalculatedLine.stream().map(Move::toString).collect(Collectors.joining(", ")));
    }


    private synchronized boolean positionChanged() {
        return engine.getBoardStateHash() != lastCalculatedHash;
    }

    private MoveAndScore getBestMove(Engine engine, boolean isWhite, int levelOfDepth, long startTime, long timeLimit) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        Move bestMove = null;
        double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        List<Move> sortedMoves = sortMovesByEfficiency(engine.getAllLegalMoves(), engine, isWhite, levelOfDepth);

        for (Move move : sortedMoves) {
            // Time check at the beginning of each loop iteration
            if (System.currentTimeMillis() - startTime > timeLimit) {
                break;
            }

            engine.performMove(move);
            double score;

            if (engine.isInStateCheckMate(!isWhite)) {
                score = isWhite ? Engine.CHECKMATE : -Engine.CHECKMATE;
            } else {
                score = alphaBeta(engine, levelOfDepth - 1, alpha, beta, !isWhite, startTime, timeLimit);
                // Check for time limit exceeded after alphaBeta call
                if (score == EXIT_FLAG || positionChanged()) {
                    engine.undoLastMove();
                    break;
                }
            }

            engine.undoLastMove();

            // Check if the current move leads to a better score
            if (isBetterScore(isWhite, score, bestScore)) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove != null ? new MoveAndScore(bestMove, bestScore) : null;
    }

    /**
     * Checks if the current score is better than the best score based on the player's color.
     */
    private boolean isBetterScore(boolean isWhite, double score, double bestScore) {
        return isWhite ? score > bestScore : score < bestScore;
    }


    /**
     * *
     * 5rkr/pp2Rp2/1b1p1Pb1/3P2Q1/2n3P1/2p5/P4P2/4R1K1 w - - 1 0
     * *
     */
    private double alphaBeta(Engine engine, int depth, double alpha, double beta, boolean isWhite, long startTime, long timeLimit) {

        // Check for time limit exceeded
        if (System.currentTimeMillis() - startTime > timeLimit) {
            return EXIT_FLAG;
        }

        long boardHash = engine.getBoardStateHash();

        if (depth == 0 || engine.isGameOver()) {
            return engine.evaluateBoard(isWhite);
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

        if (isWhite) {
            return maximizer(engine, depth, alpha, beta, isWhite, boardHash, alphaOriginal, moves, startTime, timeLimit);
        } else {
            return minimizer(engine, depth, alpha, beta, isWhite, boardHash, betaOriginal, moves, startTime, timeLimit);
        }
    }


    private double maximizer(Engine engine, int depth, double alpha, double beta, boolean isWhite, long boardHash, double alphaOriginal, List<Move> moves, long startTime, long timeLimit) {
        double maxEval = Double.NEGATIVE_INFINITY;
        Move bestMoveAtThisNode = null; // Variable to track the best move at this node

        for (Move move : sortMovesByEfficiency(moves, engine, isWhite, depth)) {
            engine.performMove(move);
            long newBoardHash = engine.getBoardStateHash();

            double eval;
            TranspositionTableEntry entry = transpositionTable.get(newBoardHash);

            if (entry != null && entry.depth >= depth) {
                eval = entry.score; // Use the score from the transposition table
            } else {
                eval = alphaBeta(engine, depth - 1, alpha, beta, !isWhite, startTime, timeLimit);

                if (eval == EXIT_FLAG || positionChanged()) {
                    // If time limit exceeded, exit the loop
                    engine.undoLastMove();
                    return EXIT_FLAG;
                }
            }

            engine.undoLastMove();

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
                             boolean isWhite, long boardHash,
                             double betaOriginal, List<Move> moves, long startTime,
                             long timeLimit) {
        double minEval = Double.POSITIVE_INFINITY;
        Move bestMoveAtThisNode = null; // Track the best move at this node

        for (Move move : sortMovesByEfficiency(moves, engine, isWhite, depth)) {
            engine.performMove(move);
            long newBoardHash = engine.getBoardStateHash();

            double eval;
            TranspositionTableEntry entry = transpositionTable.get(newBoardHash);

            if (entry != null && entry.depth >= depth) {
                eval = entry.score;
            } else {
                eval = alphaBeta(engine, depth - 1, alpha, beta, !isWhite, startTime, timeLimit);

                if (eval == EXIT_FLAG || positionChanged()) {
                    engine.undoLastMove();
                    return EXIT_FLAG;
                }
            }

            engine.undoLastMove();

            if (eval < minEval) {
                minEval = eval;
                bestMoveAtThisNode = move; // Update the best move at this node
            }

            beta = Math.min(beta, eval);
            if (alpha >= beta) {
                break;
            }
        }

        if (minEval >= betaOriginal) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.LOWERBOUND, bestMoveAtThisNode));
        } else if (minEval <= alpha) {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.UPPERBOUND, bestMoveAtThisNode));
        } else {
            transpositionTable.put(boardHash, new TranspositionTableEntry(minEval, depth, NodeType.EXACT, bestMoveAtThisNode));
        }

        return minEval;
    }


    private ArrayList<Move> sortMovesByEfficiency(List<Move> moves, Engine engine, boolean isWhite, int currentDepth) {
        PriorityQueue<Move> sortedMoves = new PriorityQueue<>(
                Comparator.comparingDouble((Move move) -> {
                    TranspositionTableEntry entry = transpositionTable.get(engine.getBoardStateHashAfterMove(move));
                    if (entry != null && entry.depth >= currentDepth) {
                        return isWhite ? entry.score : -entry.score;
                    } else {
                        engine.performMove(move);
                        double score = engine.evaluateBoard(isWhite);
                        engine.undoLastMove();
                        return score;
                    }
                }).reversed()
        );

        sortedMoves.addAll(moves);
        return new ArrayList<>(sortedMoves);
    }


}
