package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
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

import static julius.game.chessengine.helper.BitHelper.bitIndex;

@Service
@Log4j2
public class Engine {

    public static final double CHECKMATE = 1000000;
    private boolean legalMovesNeedUpdate = true;
    private List<Move> legalMoves = new ArrayList<>();
    @Getter
    private LinkedList<Move> line = new LinkedList<>();
    private BitBoard bitBoard = new BitBoard();
    @Getter
    private GameState gameState = new GameState();

    public Engine() {
        startNewGame();
    }

    public Engine(BitBoard b, LinkedList<Move> m, List<Move> l) {
        bitBoard = new BitBoard(b);
        gameState = new GameState();
        line = m;
        legalMoves = l;
    }

    public List<Move> getAllLegalMoves() {
        if (legalMovesNeedUpdate) {
            generateLegalMoves();
            legalMovesNeedUpdate = false; // Reset flag after generating
        }
        return this.legalMoves;
    }

    public void performMove(Move move) {
        this.bitBoard.performMove(move, true);
        updateGameState();
        legalMovesNeedUpdate = true; // Set flag
        line.add(move);
    }

    public void undoMove(Move move, boolean scoreNeedsUpdate) {
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
        // Deep copy the legalMoves list
        List<Move> copiedLegalMoves = new ArrayList<>(legalMoves.size());
        for (Move move : legalMoves) {
            // Assuming Move class is properly cloneable. If not, you need to create a new Move instance
            // with the same properties as the original move.
            copiedLegalMoves.add(new Move(move));
        }

        // Now use the deep copied list in the new Engine instance
        return new Engine(bitBoard, new LinkedList<>(line), copiedLegalMoves);
    }

    public void startNewGame() {
        bitBoard = new BitBoard();
        gameState = new GameState();
        legalMovesNeedUpdate = true;
    }

    public int counter = 0;

    private void generateLegalMoves() {
        this.legalMoves = bitBoard.getAllCurrentPossibleMoves()
                .stream()
                .filter(move -> isLegalMove(bitBoard, move))
                .collect(Collectors.toList());
        counter++;
    }

    // Each of these methods would need to be implemented to handle the specific move generation for each piece type.
    public List<Move> getMovesFromPosition(Position fromPosition) {
        return getAllLegalMoves().stream()
                .filter(move -> move.getFrom().equals(fromPosition))
                .collect(Collectors.toList());
    }

    public GameState moveRandomFigure(boolean isWhite) {
        // Now, the color parameter is used to determine which moves to generate
        List<Move> moves = getAllLegalMoves();

        if (moves.isEmpty()) {
            throw new RuntimeException("No moves possible for " + (isWhite ? "White" : "Black"));
        }

        Random rand = new Random();
        Move randomMove = moves.get(rand.nextInt(moves.size()));

        if (randomMove.isEnPassantMove()) {
            // Clear the captured pawn from its position for en passant
            bitBoard.clearSquare(bitIndex(randomMove.getTo().getX(), randomMove.getFrom().getY()), !isWhite);
        }

        // Execute the move on the bitboard
        performMove(randomMove);

        // Update the game state
        updateGameState();

        return gameState;
    }

    public GameState moveFigure(Position fromPosition, Position toPosition) {
        return moveFigure(bitBoard, fromPosition, toPosition);
    }

    public GameState moveFigure(BitBoard bitBoard, Position fromPosition, Position toPosition) {
        // Determine the piece type and color from the bitboard based on the 'from' position
        PieceType pieceType = bitBoard.getPieceTypeAtPosition(fromPosition);
        Color color = bitBoard.getPieceColorAtPosition(fromPosition);

        if (pieceType == null || color == null) {
            throw new IllegalStateException("No piece at the starting position");
        }

        // Check if it's the correct player's turn
        Color pieceColor = bitBoard.getPieceColorAtPosition(fromPosition);
        if ((pieceColor == Color.WHITE && !bitBoard.whitesTurn) || (pieceColor == Color.BLACK && bitBoard.whitesTurn)) {
            bitBoard.logBoard();
            throw new IllegalStateException("It's not " + pieceColor + "'s turn");
        }

        Move move = getAllLegalMoves().stream()
                .filter(m -> m.getFrom().equals(fromPosition) && m.getTo().equals(toPosition))
                .findAny().orElseThrow(() -> new IllegalStateException("Move not found"));


        // Perform the move on the bitboard
        performMove(move);

        // Update the game state
        updateGameState();


        return gameState;
    }


    private boolean isLegalMove(BitBoard bitBoard, Move move) {
        // Check if the move is within bounds of the board
        if (!isMoveOnBoard(move)) {
            return false;
        }

        BitBoard testBoard = simulateMove(bitBoard, move);
        return !testBoard.isInCheck(move.isColorWhite());
    }

    private BitBoard simulateMove(BitBoard bitBoard, Move move) {
        // Create a deep copy of the BitBoard object to avoid mutating the original board.
        BitBoard boardCopy = new BitBoard(bitBoard);

        // Perform the move on the copied board.
        boardCopy.performMove(move, false);

        // Return the new board state.
        return boardCopy;
    }

    private boolean isMoveOnBoard(Move move) {
        // Check if the 'from' position is on the board
        if (move.getFrom().getX() < 'a' || move.getFrom().getX() > 'h' ||
                move.getFrom().getY() < 1 || move.getFrom().getY() > 8) {
            return false;
        }

        // Check if the 'to' position is on the board
        return move.getTo().getX() >= 'a' && move.getTo().getX() <= 'h' &&
                move.getTo().getY() >= 1 && move.getTo().getY() <= 8;

        // If both positions are within the valid range, the move is on the board
    }

    public List<Position> getPossibleMovesForPosition(Position fromPosition) {
        return getMovesFromPosition(fromPosition).stream()
                .map(Move::getTo)
                .collect(Collectors.toList());
    }

    private boolean isNotInCheckAfterMove(BitBoard board, Move move) {
        BitBoard testBoard = simulateMove(board, move);
        return !testBoard.isInCheck(move.isColorWhite());
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
        return isInStateCheckMate(bitBoard, isWhite);
    }

    public boolean isInStateCheckMate(BitBoard board, boolean isWhite) {
        // First, check if the king is in check.
        boolean isInCheck = board.isInCheck(isWhite);

        // Then, generate all possible moves for the player.
        List<Move> possibleMoves = board.getAllCurrentPossibleMoves();

        // Filter out the moves that would leave the king in check after they are made.
        // Note: You need to implement a method that checks if making a certain move would result in check.
        long legalMovesCount = possibleMoves.stream()
                .filter(move -> isNotInCheckAfterMove(board, move))
                .count();

        // Checkmate occurs when the king is in check and there are no legal moves left.
        return isInCheck && legalMovesCount == 0;
    }

    private void updateGameState() {
        if (getAllLegalMoves().isEmpty() && isInStateCheckMate(bitBoard, true)) {
            gameState.setState("BLACK WON");
        } else if (getAllLegalMoves().isEmpty() && isInStateCheckMate(bitBoard, false)) {
            gameState.setState("WHITE WON");
        } else if (getAllLegalMoves().isEmpty()) {
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
        boolean noLegalMoves = getAllLegalMoves().isEmpty();
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

    public Long getBoardStateHashAfterMove(Move move) {
        // Step 1: Create a deep copy of the current board state
        BitBoard boardCopy = new BitBoard(this.bitBoard);

        // Step 2: Simulate the move on the copied board
        boardCopy.performMove(move, false); // Assuming 'false' means no need to update the score

        // Step 3: Return the computed hash
        return boardCopy.getBoardStateHash();
    }
}
