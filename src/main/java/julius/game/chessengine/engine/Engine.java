package julius.game.chessengine.engine;

import julius.game.chessengine.ai.CaptureTranspositionTableEntry;
import julius.game.chessengine.board.*;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Log4j2
public class Engine {

    private static final ConcurrentHashMap<Long, CaptureTranspositionTableEntry> captureTranspositionTable = new ConcurrentHashMap<>();

    public static final double CHECKMATE = 100000;
    private boolean legalMovesNeedUpdate = true;
    private MoveList legalMoves;

    @Getter
    private ArrayList<Integer> line = new ArrayList<>();
    private BitBoard bitBoard = new BitBoard();
    @Getter
    private GameState gameState = new GameState(bitBoard);

    public Engine() {
        startNewGame();
    }

    public Engine(BitBoard b, ArrayList<Integer> m, MoveList l, GameState g) {
        bitBoard = new BitBoard(b);
        gameState = new GameState(g);
        line = m;
        legalMoves = l;
    }

    public MoveList getAllLegalMoves() {
        if (legalMovesNeedUpdate) {
            generateLegalMoves();
            legalMovesNeedUpdate = false; // Reset flag after generating
        }
        return this.legalMoves;
    }

    public void performMove(int move) {
        if(!gameState.isGameOver()) {
            bitBoard.performMove(move);
            generateLegalMoves();
            gameState.update(bitBoard, legalMoves, move);
            line.add(move);
        }
    }

    public void importBoardFromFen(String fen) {
        this.bitBoard = FEN.translateFENtoBitBoard(fen);
        this.gameState = new GameState(bitBoard);
        generateLegalMoves();
        gameState.updateState(bitBoard, legalMoves);
    }

    public synchronized Engine createSimulation() {
        ArrayList<Integer> newLine = new ArrayList<>(line); // Safe copy
        return new Engine(bitBoard, newLine, legalMoves, gameState);
    }

    public void startNewGame() {
        bitBoard = new BitBoard();
        gameState = new GameState(bitBoard);
        legalMovesNeedUpdate = true;
        line = new ArrayList<>();
    }

    private void generateLegalMoves() {
        this.legalMoves = new MoveList();

        if(gameState.isGameOver()) {
            return;
        }

        MoveList moves = bitBoard.getAllCurrentPossibleMoves();

        for (int i = 0; i < moves.size(); i++) {
            int m = moves.getMove(i);
            if (isLegalMove(m)) {
                this.legalMoves.add(m);
            }
        }
    }

    // Each of these methods would need to be implemented to handle the specific move generation for each piece type.
    public List<Move> getMovesFromIndex(int fromIndex) {

        MoveList legalMoves = getAllLegalMoves();

        List<Move> movesFromIndex = new ArrayList<>();

        for (int i = 0; i < legalMoves.size(); i++) {
            int m = legalMoves.getMove(i);
            int from = m & 0x3F; // Extract the first 6 bits
            if (from == fromIndex) {
                movesFromIndex.add(Move.convertIntToMove(m));
            }
        }

        return movesFromIndex;
    }

    public GameState moveRandomFigure(boolean isWhite) {
        // Now, the color parameter is used to determine which moves to generate
        MoveList moves = getAllLegalMoves();

        if (moves.size() == 0) {
            throw new RuntimeException("No moves possible for " + (isWhite ? "White" : "Black"));
        }

        Random rand = new Random();
        int randomMove = moves.getMove(rand.nextInt(moves.size()));

        // Execute the move on the bitboard
        performMove(randomMove);

        return gameState;
    }

    public GameState moveFigure(int fromIndex, int toIndex) {
        return moveFigure(bitBoard, fromIndex, toIndex);
    }

    public GameState moveFigure(BitBoard bitBoard, int fromIndex, int toIndex) {
        // Determine the piece type and color from the bitboard based on the 'from' position
        PieceType pieceType = bitBoard.getPieceTypeAtIndex(fromIndex);
        Color color = bitBoard.getPieceColorAtIndex(fromIndex);

        if (pieceType == null || color == null) {
            throw new IllegalStateException("No piece at the starting position");
        }

        // Check if it's the correct player's turn
        Color pieceColor = bitBoard.getPieceColorAtIndex(fromIndex);
        if ((pieceColor == Color.WHITE && !bitBoard.whitesTurn) || (pieceColor == Color.BLACK && bitBoard.whitesTurn)) {
            bitBoard.logBoard();
            throw new IllegalStateException("It's not " + pieceColor + "'s turn");
        }

        MoveList legalMoves = getAllLegalMoves();

        int move = -1;

        for (int i = 0; i < legalMoves.size(); i++) {
            int m = legalMoves.getMove(i);
            int from = m & 0x3F; // Extract the first 6 bits
            int to = (m >> 6) & 0x3F; // Extract the next 6 bits

            if (from == fromIndex && to == toIndex) {
                move = m;
            }
        }

        if (move == -1) {
            log.warn("Move not legal!");
        } else {
            // Perform the move on the bitboard
            performMove(move);
        }

        return gameState;
    }


    private boolean isLegalMove(int move) {
        // Check if the move is within bounds of the board
        boolean isWhite = (move & (1 << 15)) != 0;

        BitBoard testBoard = simulateMove(bitBoard, move);
        return !testBoard.isInCheck(isWhite);
    }

    private BitBoard simulateMove(BitBoard bitBoard, int move) {
        // Create a deep copy of the BitBoard object to avoid mutating the original board.
        BitBoard boardCopy = new BitBoard(bitBoard);

        // Perform the move on the copied board.
        boardCopy.performMove(move);

        // Return the new board state.
        return boardCopy;
    }

    public List<Position> getPossibleMovesForPosition(int fromIndex) {
        return getMovesFromIndex(fromIndex).stream()
                .map(Move::getTo)
                .collect(Collectors.toList());
    }



    public synchronized long getBoardStateHash() {
        return bitBoard.getBoardStateHash();
    }


    public void logBoard() {
        bitBoard.logBoard();
    }

    public boolean whitesTurn() {
        return bitBoard.whitesTurn;
    }

    public synchronized void undoLastMove() {
        if (!line.isEmpty()) {
            gameState.undo(bitBoard.getBoardStateHash());
            this.bitBoard.undoMove(line.getLast());
            gameState.updateScore(bitBoard, legalMoves, line.getLast());
            generateLegalMoves();
            line.removeLast();
        }
    }

    public FEN translateBoardToFen() {
        return FEN.translateBoardToFEN(bitBoard);
    }



    public double evaluateBoard(boolean isWhitesTurn) {
        if (gameState.isInStateCheckMate()) {
            return CHECKMATE;
        }

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        long boardStateHash = getBoardStateHash();
        CaptureTranspositionTableEntry entry = captureTranspositionTable.get(boardStateHash);

        // Check if the entry exists and is relevant for the current search
        if (entry != null && entry.isWhite() == isWhitesTurn) {
            return entry.getScore();
        }

        double score = quiescenceSearch(isWhitesTurn, alpha, beta);

        captureTranspositionTable.put(boardStateHash, new CaptureTranspositionTableEntry(score, isWhitesTurn));
        return score;
    }

    private double quiescenceSearch(boolean isWhitesTurn, double alpha, double beta) {
        MoveList moves = getPossibleCaptures();

        if (moves.size() == 0) {
            return evaluateStaticPosition(isWhitesTurn);
        }

        double standPat = evaluateStaticPosition(isWhitesTurn);
        if (standPat >= beta) {
            return beta;
        }
        if (alpha < standPat) {
            alpha = standPat;
        }

        for (int i = 0; i < moves.size(); i++) {
            performMove(moves.getMove(i));
            double score = -quiescenceSearch(!isWhitesTurn, -beta, -alpha);
            undoLastMove();

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }


    private double evaluateStaticPosition(boolean isWhitesTurn) {
        //logBoard();
        // Assuming getScore().getScoreDifference() returns a score from white's perspective
        double scoreDifference = gameState.getScore().getScoreDifference() / 1000.0;
        return isWhitesTurn ? scoreDifference : -scoreDifference;
    }

    private MoveList getPossibleCaptures() {
        MoveList captures = new MoveList();
        MoveList moves = getAllLegalMoves();

        for (int i = 0; i < moves.size(); i++) {
            int m = moves.getMove(i);
            if (isCapture(m)) {
                captures.add(m);
            }
        }
        return captures;
    }

    private boolean isCapture(int m) {
        return (((m >> 16) & 0x03) & 0x01) != 0;
    }


    public Long getBoardStateHashAfterMove(int move) {
        // Step 1: Create a deep copy of the current board state
        BitBoard boardCopy = new BitBoard(this.bitBoard);

        // Step 2: Simulate the move on the copied board
        boardCopy.performMove(move); // Assuming 'false' means no need to update the score

        // Step 3: Return the computed hash
        return boardCopy.getBoardStateHash();
    }

}
