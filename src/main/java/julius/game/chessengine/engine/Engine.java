package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
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

    private boolean legalMovesNeedUpdate = true;
    private List<Move> legalMoves = new ArrayList<>();
    private LinkedList<Move> moves = new LinkedList<>();
    private BitBoard bitBoard = new BitBoard();
    private GameState gameState = new GameState();

    public Engine() {
        startNewGame();
    }

    public Engine(BitBoard b, LinkedList<Move> m, List<Move> l) {
        bitBoard = b;
        gameState = new GameState();
        moves = m;
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
    }

    public void undoMove(Move move, boolean scoreNeedsUpdate) {
        this.bitBoard.undoMove(move, scoreNeedsUpdate);
        updateGameState();
        legalMovesNeedUpdate = true; // Set flag
    }

    public void importBoardFromFen(String fen) {
        this.bitBoard = FEN.translateFENtoBitBoard(fen);
        legalMovesNeedUpdate = true; // Set flag
    }

    public Engine createSimulation() {
        return new Engine(new BitBoard(bitBoard), moves, legalMoves);
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

    public GameState moveRandomFigure(Color color) {
        // Now, the color parameter is used to determine which moves to generate
        List<Move> moves = getAllLegalMoves();

        if (moves.isEmpty()) {
            throw new RuntimeException("No moves possible for " + color);
        }

        Random rand = new Random();
        Move randomMove = moves.get(rand.nextInt(moves.size()));

        if (randomMove.isEnPassantMove()) {
            // Clear the captured pawn from its position for en passant
            bitBoard.clearSquare(bitIndex(randomMove.getTo().getX(), randomMove.getFrom().getY()), Color.getOpponentColor(color));
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

        moves.add(move);
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
        return !testBoard.isInCheck(move.getColor());
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
        return !testBoard.isInCheck(move.getColor());
    }

    public boolean isInStateCheck(Color color) {
        // The BitBoard class already has a method to check if a king is in check
        return isInStateCheck(this.bitBoard, color);
    }

    private boolean isInStateCheck(BitBoard board, Color color) {
        // The BitBoard class already has a method to check if a king is in check
        return board.isInCheck(color);
    }

    public boolean isInStateCheckMate(Color color) {
        return isInStateCheckMate(bitBoard, color);
    }

    public boolean isInStateCheckMate(BitBoard board, Color color) {
        // First, check if the king is in check.
        boolean isInCheck = board.isInCheck(color);

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
        if (getAllLegalMoves().size() == 0 && isInStateCheck(bitBoard, Color.WHITE) && bitBoard.whitesTurn) {
            gameState.setState("BLACK WON");
        } else if (getAllLegalMoves().size() == 0 && isInStateCheck(bitBoard, Color.BLACK) && !bitBoard.whitesTurn) {
            gameState.setState("WHITE WON");
        } else if (getAllLegalMoves().size() == 0) {
            gameState.setState("DRAW");
        }
    }
    public long getBoardStateHash() {
        return bitBoard.getBoardStateHash();
    }


    public void logBoard() {
        bitBoard.logBoard();
    }

    public boolean whitesTurn() {
        return bitBoard.whitesTurn;
    }

    public void undoLastMove() {
        if (moves.size() > 0) {
            bitBoard.undoMove(moves.getLast(), true);
            moves.removeLast();
        }
        generateLegalMoves();
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Score getScore() {
        return bitBoard.getScore();
    }

    public FEN translateBoardToFen() {
        return FEN.translateBoardToFEN(bitBoard);
    }
}
