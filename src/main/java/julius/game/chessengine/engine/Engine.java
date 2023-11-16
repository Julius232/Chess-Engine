package julius.game.chessengine.engine;

import julius.game.chessengine.board.*;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Log4j2
public class Engine {

    public static final double CHECKMATE = 1000000;
    private boolean legalMovesNeedUpdate = true;
    private MoveList legalMoves;

    @Getter
    private LinkedList<Integer> line = new LinkedList<>();
    private BitBoard bitBoard = new BitBoard();
    @Getter
    private GameState gameState = new GameState();

    public Engine() {
        startNewGame();
    }

    public Engine(BitBoard b, LinkedList<Integer> m, MoveList l) {
        bitBoard = new BitBoard(b);
        gameState = new GameState();
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
        this.bitBoard.performMove(move, true);
        updateGameState();
        legalMovesNeedUpdate = true; // Set flag
        line.add(move);
    }

    public void undoMove(int move, boolean scoreNeedsUpdate) {
        this.bitBoard.undoMove(move, scoreNeedsUpdate);
        updateGameState();
        legalMovesNeedUpdate = true; // Set flag
        line.removeLast();
    }

    public void importBoardFromFen(String fen) {
        this.bitBoard = FEN.translateFENtoBitBoard(fen);
        this.gameState = new GameState();
        generateLegalMoves();
        updateGameState();
    }

    public Engine createSimulation() {
        // Now use the deep copied list in the new Engine instance
        return new Engine(bitBoard, new LinkedList<>(line), legalMoves);
    }

    public void startNewGame() {
        bitBoard = new BitBoard();
        gameState = new GameState();
        legalMovesNeedUpdate = true;
    }

    public int counter = 0;

    private void generateLegalMoves() {
        this.legalMoves = new MoveList();
        MoveList moves = bitBoard.getAllCurrentPossibleMoves();

        for (int i = 0; i < moves.size(); i++) {
            int m = moves.getMove(i);
            if (isLegalMove(m)) {
                this.legalMoves.add(m);
            }
        }

        counter++;
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

        // Update the game state
        updateGameState();

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
        }
        else {
            // Perform the move on the bitboard
            performMove(move);

            // Update the game state
            updateGameState();
        }

        return gameState;
    }


    private boolean isLegalMove(int move) {
        // Check if the move is within bounds of the board
        if (!isMoveOnBoard(move)) {
            return false;
        }
        boolean isWhite = (move & (1 << 15)) != 0;

        BitBoard testBoard = simulateMove(bitBoard, move);
        return !testBoard.isInCheck(isWhite);
    }

    private BitBoard simulateMove(BitBoard bitBoard, int move) {
        // Create a deep copy of the BitBoard object to avoid mutating the original board.
        BitBoard boardCopy = new BitBoard(bitBoard);

        // Perform the move on the copied board.
        boardCopy.performMove(move, false);

        // Return the new board state.
        return boardCopy;
    }

    private boolean isMoveOnBoard(int move) {
        int fromIndex = move & 0x3F; // Extract the first 6 bits
        int toIndex = (move >> 6) & 0x3F; // Extract the next 6 bits
        return (fromIndex >= 0 && fromIndex <= 63) && (toIndex >= 0 && toIndex <= 63);
    }

    public List<Position> getPossibleMovesForPosition(int fromIndex) {
        return getMovesFromIndex(fromIndex).stream()
                .map(Move::getTo)
                .collect(Collectors.toList());
    }

    private boolean isNotInCheckAfterMove(BitBoard board, int move) {
        BitBoard testBoard = simulateMove(board, move);
        boolean isWhite = (move & (1 << 15)) != 0;
        return !testBoard.isInCheck(isWhite);
    }

    public boolean isInStateCheck(boolean isWhite) {
        // The BitBoard class already has a method to check if a king is in check
        return isInStateCheck(this.bitBoard, isWhite);
    }

    private boolean isInStateCheck(BitBoard board, boolean isWhite) {
        // The BitBoard class already has a method to check if a king is in check
        return board.isInCheck(isWhite);
    }


    public boolean isInStateCheckMate(boolean isWhite) {
        // Then, generate all possible moves for the player.
        MoveList possibleMoves = getAllLegalMoves();

        // Filter out the moves that would leave the king in check after they are made.
        // Note: You need to implement a method that checks if making a certain move would result in check.

        // Checkmate occurs when the king is in check and there are no legal moves left.
        return isInStateCheck(isWhite) && possibleMoves.size() == 0;
    }

    private void updateGameState() {
        if (getAllLegalMoves().size() == 0 && isInStateCheckMate(true)) {
            gameState.setState("BLACK WON");
        } else if (getAllLegalMoves().size() == 0 && isInStateCheckMate(false)) {
            gameState.setState("WHITE WON");
        } else if (getAllLegalMoves().size() == 0) {
            gameState.setState("DRAW");
        }
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

    public void undoLastMove() {
        if (!line.isEmpty()) {
            bitBoard.undoMove(line.getLast(), true);
            line.removeLast();
        }
        generateLegalMoves();
    }

    public Score getScore() {
        return bitBoard.getScore();
    }

    public FEN translateBoardToFen() {
        return FEN.translateBoardToFEN(bitBoard);
    }

    public boolean isGameOver() {
        boolean noLegalMoves = getAllLegalMoves().size() == 0;
        boolean isInCheck = isInStateCheck(bitBoard, bitBoard.whitesTurn);

        // Checkmate condition
        if (noLegalMoves && isInCheck) {
            return true;
        }

        // Stalemate condition
        if (noLegalMoves) {
            return true;
        }

        // Draw by insufficient material (more complex rules like threefold repetition are not covered here)
        return isDrawByInsufficientMaterial();
    }

    private boolean isDrawByInsufficientMaterial() {
        // Implement logic to check for draw due to insufficient material
        // For instance, only kings left, king and bishop vs king, king and knight vs king, etc.
        // You need to inspect the bitboards to determine the material left on the board.
        return false; // Placeholder
    }

    public double evaluateBoard(boolean isWhite) {
        if (isInStateCheckMate(isWhite)) {
            return isWhite ? -CHECKMATE : CHECKMATE;
        }
        // Implement your board evaluation logic here.
        // This should return a score based on material, position, and other chess strategies.
        return getScore().getScoreDifference() / 100.0;
    }

    public Long getBoardStateHashAfterMove(int move) {
        // Step 1: Create a deep copy of the current board state
        BitBoard boardCopy = new BitBoard(this.bitBoard);

        // Step 2: Simulate the move on the copied board
        boardCopy.performMove(move, false); // Assuming 'false' means no need to update the score

        // Step 3: Return the computed hash
        return boardCopy.getBoardStateHash();
    }
}
