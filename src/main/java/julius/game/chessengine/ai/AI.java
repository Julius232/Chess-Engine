package julius.game.chessengine.ai;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.MoveList;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Component
public class AI {

    @Getter
    private final Engine engine;

    private static final double EXIT_FLAG = Double.MAX_VALUE;
    private static final ConcurrentHashMap<Long, TranspositionTableEntry> transpositionTable = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;
    private Thread calculationThread;

    private volatile boolean keepCalculating = true;
    private volatile long lastCalculatedHash = -1;

    @Getter
    private List<MoveAndScore> calculatedLine = Collections.synchronizedList(new ArrayList<>());

    // Game configuration parameters
    private final int maxDepth = 18; // Adjust the level of depth according to your requirements
    private final long timeLimit = 20000; // milliseconds


    public AI(Engine engine) {
        this.engine = engine;
    }

    private void startCalculationThread() {
        keepCalculating = true;
        calculationThread = new Thread(this::calculateLine);
        calculationThread.start();
    }

    public void reset() {
        engine.startNewGame();
        stopCalculation();
    }

    public void stopCalculation() {
        keepCalculating = false;
        if (calculationThread != null) {
            calculationThread.interrupt();
            try {
                calculationThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                log.error("Thread interruption error", e);
            }
        }
        calculatedLine = Collections.synchronizedList(new ArrayList<>());
    }

    public void startAutoPlay() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Ensure previous scheduler is stopped
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();

        startCalculationThread();
        scheduler.scheduleAtFixedRate(() -> {
            log.debug("state: {}, keepCalculating: {}", engine.getGameState().getState(), keepCalculating);
            if (engine.getGameState().isGameOver() || !keepCalculating) {
                stopCalculation();
                scheduler.shutdown();
                return;
            }
            performMove();
        }, 0, timeLimit, TimeUnit.MILLISECONDS);
    }

    public GameState performMove() {
        if (calculatedLine.isEmpty()) {
            engine.logBoard();
            // If the calculatedLine is empty, log an error and return the current game state without making a move.
            log.error("Calculated line is empty. Unable to perform a move.");
            log.error("lastCalculatedHash {}, currentBoardHash {}", lastCalculatedHash, engine.getBoardStateHash());
            log.error("WhitesTurn = " + engine.whitesTurn());
            log.error("Gamestate = " + engine.getGameState());
            return engine.getGameState(); // Return the current state without making a move
        }

        MoveAndScore aiMove = calculatedLine.remove(0); // Retrieve and remove the move from the list
        if (aiMove == null || aiMove.getMove() == -1) {
            // If the move is null or invalid, log an error and return the current game state without making a move.
            log.error("Calculated move is null or invalid.");
            log.error("");
            return engine.getGameState(); // Return the current state without making a move
        }

        engine.performMove(aiMove.getMove());
        return engine.getGameState();
    }

    private void calculateLine() {
        log.debug("keepCalculating: {}, interrupted: {}", keepCalculating, Thread.currentThread().isInterrupted());
        while (keepCalculating && !Thread.currentThread().isInterrupted()) {
            if (positionChanged()) {
                performCalculation();
            }
        }
    }

    private void performCalculation() {
        log.debug(" --- TranspositionTable[{}] --- ", transpositionTable.size());
        Engine simulation = engine.createSimulation();
        long boardStateHash = simulation.getBoardStateHash();
        lastCalculatedHash = boardStateHash;
        boolean isWhite = simulation.whitesTurn();

        long startTime = System.currentTimeMillis();
        calculateBestMove(simulation, boardStateHash, isWhite, startTime);
    }

    private void calculateBestMove(Engine simulation, long boardStateHash, boolean isWhite, long startTime) {
        double bestScore = isWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        int bestMove;

        for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
            if (!keepCalculating) break;
            MoveAndScore moveAndScore = getBestMove(simulation, isWhite, currentDepth, startTime, timeLimit);

            if (moveAndScore != null && isNewBestMove(moveAndScore, bestScore, isWhite)) {
                bestScore = moveAndScore.score;
                bestMove = moveAndScore.move;
                transpositionTable.put(boardStateHash, new TranspositionTableEntry(moveAndScore.score, currentDepth, NodeType.EXACT, bestMove));
                fillCalculatedLine(simulation);
            }

            if (timeLimitExceeded(startTime)) {
                break;
            }

            if (Thread.interrupted()) {
                break;
            }
        }
    }

    private boolean isNewBestMove(MoveAndScore moveAndScore, double currentBestScore, boolean isWhite) {
        double score = moveAndScore.score;
        return (isWhite ? score > currentBestScore : score < currentBestScore);
    }

    private boolean timeLimitExceeded(long startTime) {
        return System.currentTimeMillis() - startTime > timeLimit;
    }

    private void fillCalculatedLine(Engine simulation) {
        long currentBoardHash = simulation.getBoardStateHash();
        List<MoveAndScore> newCalculatedLine = new LinkedList<>();

        if (!transpositionTable.containsKey(currentBoardHash)) {
            log.info("[{}] hash not exists", currentBoardHash);
        }

        if (transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove == -1) {
            log.info("[{}] hash exists but move: " + transpositionTable.get(currentBoardHash), currentBoardHash);
        }

        //check for repetition otherwise it gets caught in a while loop
        Set<Long> seenBoardHashes = new HashSet<>();

        // Your existing if conditions here

        while (transpositionTable.containsKey(currentBoardHash) && transpositionTable.get(currentBoardHash).bestMove != -1) {
            if (!seenBoardHashes.add(currentBoardHash)) { // Check for repetition
                log.info("Repetition detected, breaking out of the loop.");
                break; // Break the loop if we have seen this board hash before
            }

            log.debug("[{}] hash exists and move: {}", currentBoardHash, transpositionTable.get(currentBoardHash));
            TranspositionTableEntry entry = transpositionTable.get(currentBoardHash);
            newCalculatedLine.add(0, new MoveAndScore(entry.bestMove, entry.score)); // Add at the beginning to maintain the order
            simulation.performMove(entry.bestMove);
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

        log.debug("Move Line: {}", newCalculatedLine.stream()
                .map(i -> Move.convertIntToMove(i.move).toString())
                .collect(Collectors.joining(", ")));
    }


    private synchronized boolean positionChanged() {
        return engine.getBoardStateHash() != lastCalculatedHash;
    }

    private MoveAndScore getBestMove(Engine engine, boolean isWhitesTurn, int levelOfDepth, long startTime, long timeLimit) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        int bestMove = -1; // Use an integer to represent the best move
        double bestScore = isWhitesTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        ArrayList<Integer> sortedMoves = sortMovesByEfficiency(engine.getAllLegalMoves(), engine, isWhitesTurn, levelOfDepth);

        for (int moveInt : sortedMoves) {

            // Time check at the beginning of each loop iteration
            if (System.currentTimeMillis() - startTime > timeLimit) {
                break;
            }

            engine.performMove(moveInt); // Perform move using its integer representation
            double score;

            if (engine.getGameState().isInStateCheckMate()) {
                score = isWhitesTurn ? Engine.CHECKMATE : -Engine.CHECKMATE;
            } else if (engine.getGameState().isInStateDraw()) {
                score = 0;
            } else {
                score = alphaBeta(engine, levelOfDepth - 1, alpha, beta, !isWhitesTurn, startTime, timeLimit);
                // Check for time limit exceeded after alphaBeta call
                if (score == EXIT_FLAG || positionChanged()) {
                    engine.undoLastMove(); // Undo move using its integer representation
                    break;
                }
            }

            engine.undoLastMove(); // Undo move using its integer representation

            // Check if the current move leads to a better score
            if (isBetterScore(isWhitesTurn, score, bestScore)) {
                bestScore = score;
                bestMove = moveInt; // Store the best move as an integer
            }
        }

        return bestMove != -1 ? new MoveAndScore(bestMove, bestScore) : null; // Return the best move and score
    }

// Note: You will need to update MoveAndScore class to handle move as an integer.


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

        if (engine.getGameState().isInStateDraw()) {
            return 0;
        }

        if (depth == 0 || engine.getGameState().isInStateCheckMate()) {
            double eval = engine.evaluateBoard(isWhite);
            if (!isWhite) {
                eval = -eval;
            }
            return eval;
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

        MoveList moves = engine.getAllLegalMoves();

        if (isWhite) {
            return maximizer(engine, depth, alpha, beta, isWhite, boardHash, alphaOriginal, moves, startTime, timeLimit);
        } else {
            return minimizer(engine, depth, alpha, beta, isWhite, boardHash, betaOriginal, moves, startTime, timeLimit);
        }
    }


    private double maximizer(Engine engine, int depth, double alpha, double beta, boolean isWhite, long boardHash, double alphaOriginal, MoveList moves, long startTime, long timeLimit) {
        long start = System.nanoTime(); // Start timing
        double maxEval = Double.NEGATIVE_INFINITY;
        int bestMoveAtThisNode = -1; // Variable to track the best move at this node

        for (int move : sortMovesByEfficiency(moves, engine, isWhite, depth)) {
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

            log.debug("DEPTH: " + depth + " --- " + Move.convertIntToMove(move));
            long endTime = System.nanoTime();
            log.debug("DEPTH: " + depth);
            log.debug("--> [+] Time taken for maximizer: {} ms", (endTime - start) / 1e6);

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
                             double betaOriginal, MoveList moves, long startTime,
                             long timeLimit) {
        long start = System.nanoTime(); // Start timing
        double minEval = Double.POSITIVE_INFINITY;
        int bestMoveAtThisNode = -1; // Track the best move at this node

        for (int move : sortMovesByEfficiency(moves, engine, isWhite, depth)) {
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

            long endTime = System.nanoTime();
            log.debug("DEPTH: " + depth + " --- " + Move.convertIntToMove(move));
            log.debug("<-- [-] Time taken for minimizer: {} ms", (endTime - start) / 1e6);

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


    private ArrayList<Integer> sortMovesByEfficiency(MoveList moves, Engine engine, boolean isWhite, int currentDepth) {
        //long startTime = System.nanoTime(); // Start timing
        PriorityQueue<Integer> sortedMoves = new PriorityQueue<>(
                Comparator.comparingDouble((Integer moveInt) -> {
                    Long boardStateHash = engine.getBoardStateHashAfterMove(moveInt);
                    TranspositionTableEntry entry = transpositionTable.get(boardStateHash);
                    if (entry != null && entry.depth >= currentDepth) {
                        return isWhite ? entry.score : -entry.score;
                    } else {
                        engine.performMove(moveInt);
                        double score = engine.evaluateBoard(isWhite);
                        engine.undoLastMove();
                        return score;
                    }
                }).reversed()
        );

        for (int i = 0; i < moves.size(); i++) {
            sortedMoves.add(moves.getMove(i));
        }

        ArrayList<Integer> sortedMoveList = new ArrayList<>();
        while (!sortedMoves.isEmpty()) {
            sortedMoveList.add(sortedMoves.poll());
        }
        //log.debug("DEPTH: " + currentDepth);
        //log.debug(moves + "Time taken for move sorting: {} ms", (endTime - startTime) / 1e6);
        return sortedMoveList;

    }


}
