package julius.game.chessengine.engine;

import julius.game.chessengine.ai.OpeningBook;
import julius.game.chessengine.board.*;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Log4j2
public class Engine {

    @Getter
    private OpeningBook openingBook;

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

    public Engine(Engine other) {
        this.bitBoard = new BitBoard(other.bitBoard); // Assuming BitBoard's constructor is a deep copy constructor
        this.gameState = new GameState(other.gameState); // Assuming GameState's constructor is a deep copy constructor
        this.line = new ArrayList<>(other.line);
        //this.legalMoves = new MoveList(other.legalMoves);
        this.legalMoves = other.legalMoves;
        this.legalMovesNeedUpdate = other.legalMovesNeedUpdate;
        this.openingBook = other.openingBook;
    }

    public MoveList getAllLegalMoves() {
        if (gameState.isGameOver()) {
            if (legalMoves == null) {
                legalMoves = new MoveList();  // Create only if null
            } else {
                legalMoves.clear();  // Clear existing list instead of creating a new one
            }
        } else if (legalMovesNeedUpdate) {
            generateLegalMoves();
        }
        return legalMoves;
    }

    public void performMove(int move) {
        boolean isOpeningMove = false;
        if (!gameState.isGameOver()) {
            long boardStateHashBeforeMove = getBoardStateHash();
            bitBoard.performMove(move);
            if(openingBook.containsMoveAndBoardStateHash(boardStateHashBeforeMove, move)) {
                isOpeningMove = true;
            }
            generateLegalMoves();
            gameState.update(bitBoard, legalMoves, move, isOpeningMove);
            line.add(move);
        }
    }

    public void importBoardFromFen(String fen) {
        this.bitBoard = FEN.translateFENtoBitBoard(fen);
        this.gameState = new GameState(bitBoard);
        generateLegalMoves();
        gameState.updateState(bitBoard, legalMoves, false);
    }

    public synchronized Engine createSimulation() {
        // Safe copy
        return new Engine(this);
    }

    public void startNewGame() {
        bitBoard = new BitBoard();
        gameState = new GameState(bitBoard);
        legalMovesNeedUpdate = true;
        line = new ArrayList<>();
        this.openingBook = OpeningBook.getInstance();
    }

    private void generateLegalMoves() {
        if (gameState.isGameOver()) {
            this.legalMoves = new MoveList();
            legalMovesNeedUpdate = false;
            return;
        }

        BitBoard simulation = new BitBoard(bitBoard); // Only one instance
        MoveList moves = bitBoard.getAllCurrentPossibleMoves();

        this.legalMoves = new MoveList();
        for (int i = 0; i < moves.size(); i++) {
            int move = moves.getMove(i);
            simulation.performMove(move);
            if (!simulation.isInCheck(MoveHelper.isWhitesMove(move))) {
                this.legalMoves.add(move);
            }
            simulation.undoMove(move); // Revert to original state after checking
        }
        legalMovesNeedUpdate = false;
    }

    // Each of these methods would need to be implemented to handle the specific move generation for each piece type.
    public List<Move> getMovesFromIndex(int fromIndex) {

        MoveList legalMoves = getAllLegalMoves();

        List<Move> movesFromIndex = new ArrayList<>();

        for (int i = 0; i < legalMoves.size(); i++) {
            int m = legalMoves.getMove(i);
            int from = MoveHelper.deriveFromIndex(m); // Extract the first 6 bits
            if (from == fromIndex) {
                movesFromIndex.add(Move.convertIntToMove(m));
            }
        }

        return movesFromIndex;
    }

    public void moveRandomFigure(boolean isWhite) {
        // Now, the color parameter is used to determine which moves to generate
        MoveList moves = getAllLegalMoves();

        if (moves.size() == 0) {
            throw new RuntimeException("No moves possible for " + (isWhite ? "White" : "Black"));
        }

        Random rand = new Random();
        int randomMove = moves.getMove(rand.nextInt(moves.size()));

        // Execute the move on the bitboard
        performMove(randomMove);

    }

    public GameState moveFigure(int fromIndex, int toIndex, int promotionPiece) {
        return moveFigure(bitBoard, fromIndex, toIndex, promotionPiece);
    }

    //always queen
    public void moveFigure(int fromIndex, int toIndex) {
        moveFigure(bitBoard, fromIndex, toIndex, 5);
    }

    public GameState moveFigure(BitBoard bitBoard, int fromIndex, int toIndex, int promotionPiece) {
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

        int move = getMove(fromIndex, toIndex, promotionPiece);

        if (move == -1) {
            log.warn("Move not legal!");
        } else {
            // Perform the move on the bitboard
            performMove(move);
        }

        return gameState;
    }

    private int getMove(int fromIndex, int toIndex, int promotionPiece) {
        MoveList legalMoves = getAllLegalMoves();

        int move = -1;

        for (int i = 0; i < legalMoves.size(); i++) {
            int m = legalMoves.getMove(i);
            int from = MoveHelper.deriveFromIndex(m); // Extract the first 6 bits
            int to = MoveHelper.deriveToIndex(m); // Extract the next 6 bits
            int promotionPieceTypeBits = MoveHelper.derivePromotionPieceTypeBits(m);

            if (from == fromIndex && to == toIndex && (promotionPieceTypeBits == 0 | promotionPieceTypeBits == promotionPiece)) {
                move = m;
            }
        }
        return move;
    }


    private boolean isLegalMove(BitBoard simulation, int move) {
        // Check if the move is within bounds of the board
        boolean isWhite = MoveHelper.isWhitesMove(move);
        simulation.performMove(move);
        boolean isNotInCheckAfterMove = !simulation.isInCheck(isWhite);
        simulation.undoMove(move);
        return isNotInCheckAfterMove;
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

    public void undoLastMove() {
        if (!line.isEmpty()) {
            gameState.undo(bitBoard.getBoardStateHash());
            this.bitBoard.undoMove(line.getLast());
            gameState.updateScore(bitBoard, line.getLast());
            generateLegalMoves();
            line.removeLast();
        } else {
            throw new IllegalStateException("undoLastMoveWasNotPossible, line is empty");
        }
    }

    public Integer getLastMove() {
        if (!line.isEmpty()) {
            return line.getLast();
        } else {
            return -1;
        }
    }

    public FEN translateBoardToFen() {
        return FEN.translateBoardToFEN(bitBoard);
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
