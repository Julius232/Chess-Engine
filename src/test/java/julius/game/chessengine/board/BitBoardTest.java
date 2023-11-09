package julius.game.chessengine.board;

import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static julius.game.chessengine.board.Position.convertStringToPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class BitBoardTest {

    final long[] RankMasks = new long[]{
            0x00000000000000FFL, // Rank 1
            0x000000000000FF00L, // Rank 2
            0x0000000000FF0000L, // Rank 3
            0x00000000FF000000L, // Rank 4
            0x000000FF00000000L, // Rank 5
            0x0000FF0000000000L, // Rank 6
            0x00FF000000000000L, // Rank 7
            0xFF00000000000000L  // Rank 8
    };

    final long[] FileMasks = new long[]{
            0x0101010101010101L, // File A
            0x0202020202020202L, // File B
            0x0404040404040404L, // File C
            0x0808080808080808L, // File D
            0x1010101010101010L, // File E
            0x2020202020202020L, // File F
            0x4040404040404040L, // File G
            0x8080808080808080L  // File H
    };


    @Test
    public void testDoAndUndoTenMoves() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard(); // The current state of the board from the engine

        // Lists to keep track of the moves and states
        List<Move> movesPerformed = new ArrayList<>();
        List<BitBoard> states = new ArrayList<>();

        // Do 10 moves
        for (int i = 0; i < 100; i++) {
            log.info(i + ": MOVE");
            // Assume generateAllPossibleMoves and performMove are properly defined methods
            List<Move> possibleMoves = engine.getAllLegalMoves();
            if (!possibleMoves.isEmpty()) {
                Move move = possibleMoves.get(0); // or any other move selection strategy
                board.performMove(move);
                movesPerformed.add(move);

                board.logBoard();
                states.add(new BitBoard(board)); // Store the state after the move
            }
        }

        // Now undo the 10 moves
        for (int i = movesPerformed.size() - 1; i >= 0; i--) {

            log.info(i + ": UNDO");
            board.undoMove(movesPerformed.get(i));
            board.logBoard();
        }
        BitBoard b = new BitBoard();
        board.logBoard();
        assertEquals(b, board); // Check if the board state matches the expected state
    }

    @Test
    public void checkForEngine() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e2"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e7"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("g1"), convertStringToPosition("f3"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("g8"), convertStringToPosition("f6"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("b1"), convertStringToPosition("c3"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("b8"), convertStringToPosition("c6"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("f1"), convertStringToPosition("c4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("f6"), convertStringToPosition("d5"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.WHITE);

        assertEquals(moves.size(), 35);
    }

    @Test
    public void checkForEnPassantWhiteLeft() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e2"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h7"), convertStringToPosition("h6"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e4"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("d7"), convertStringToPosition("d5"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.WHITE);

        engine.getBitBoard().logBoard();
        assertEquals(31, moves.size());
    }

    @Test
    public void scoreTest() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e2"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("d7"), convertStringToPosition("d5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e4"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("d7"), convertStringToPosition("d5"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.WHITE);

        engine.getBitBoard().logBoard();
        assertEquals(31, moves.size());
    }

    @Test
    public void checkForEnPassantWhiteRight() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e2"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h7"), convertStringToPosition("h6"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e4"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("f7"), convertStringToPosition("f5"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.WHITE);

        engine.getBitBoard().logBoard();
        assertEquals(31, moves.size());
    }

    @Test
    public void checkForEnPassantBlackLeft() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h2"), convertStringToPosition("h3"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e7"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h3"), convertStringToPosition("h4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e5"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("f2"), convertStringToPosition("f4"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.BLACK);

        engine.getBitBoard().logBoard();
        assertEquals(31, moves.size());
    }

    @Test
    public void checkForEnPassantBlackRight() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h2"), convertStringToPosition("h3"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e7"), convertStringToPosition("e5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("h3"), convertStringToPosition("h4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e5"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("d2"), convertStringToPosition("d4"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.BLACK);

        engine.getBitBoard().logBoard();
        assertEquals(31, moves.size());
    }


    @Test
    public void checkForCapturesBlack() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard();

        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("e2"), convertStringToPosition("e4"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("f7"), convertStringToPosition("f5"));
        engine.moveFigure(engine.getBitBoard(), convertStringToPosition("g2"), convertStringToPosition("g4"));

        List<Move> moves = engine.getAllPossibleMovesForPlayerColor(Color.BLACK);

        engine.getBitBoard().logBoard();
        assertEquals(22, moves.size());
    }


    @Test
    public void testPawnMovesWithBitshifting() {
        long whitePawns = 0L;
        long blackPawns = 0L;

        // Setting white pawns on the second rank
        whitePawns = 0x000000000000FF00L;
        // Setting black pawns on the seventh rank
        blackPawns = 0x00FF000000000000L;

        // Setting white pawns on the second rank
        // Move white pawns up the board by 1 rank using bitshifting
        whitePawns = whitePawns << 2;
        // Move black pawns down the board by 1 rank using bitshifting
        blackPawns = blackPawns >> 2;

        // Print out the bitboards in a human-readable form
        printBitboard(whitePawns);
        printBitboard(blackPawns);
    }

    @Test
    public void testSinglePawnDoubleStepWithBitshifting() {
        // All white pawns are on the second rank
        long whitePawns = 0x000000000000FF00L;
        System.out.println(whitePawns);

        printBitboard(whitePawns);

        // Mask for the e2 square, which we want to clear
        long eFileMask = 0x1010101010101010L;
        // Clear the e2 square by using bitwise AND with the inverse of the e2 mask
        whitePawns &= ~eFileMask;

        System.out.println(whitePawns);

        printBitboard(whitePawns);

        // Mask for the e4 square, where we want to move our pawn
        long e4Mask = 1L << 28;
        // Set the e4 square by using bitwise OR with the e4 mask
        whitePawns |= e4Mask;

        System.out.println(whitePawns);

        // Print out the bitboard in a human-readable form
        printBitboard(whitePawns);

        final long Rank4 = 0x00000000FF000000L;

        long doubleMove = whitePawns & Rank4;

        printBitboard(doubleMove);
    }

    @Test
    public void testEnPassant() {
        // All white pawns are on the second rank
        long whitePawns = 0x000000000000FF00L;
        long blackPawns = 0x00FF000000000000L;


        printBitboard(whitePawns);

        whitePawns = movePawn(2, Color.WHITE, whitePawns, 'e', false, false);
        whitePawns = movePawn(1, Color.WHITE, whitePawns, 'e', false, false);
        blackPawns = movePawn(2, Color.BLACK, blackPawns, 'd', false, false);

        printBitboard(whitePawns | blackPawns);

        blackPawns = removePawnFromPosition(new Position('d', 5), blackPawns);
        whitePawns = movePawn(1, Color.WHITE, whitePawns, 'e', true, false);

        printBitboard(blackPawns | whitePawns);

    }

    @Test
    public void testEnPassant2() {
        // All white pawns are on the second rank
        long whitePawns = 0x000000000000FF00L;
        long blackPawns = 0x00FF000000000000L;


        printBitboard(whitePawns);

        whitePawns = movePawn(2, Color.WHITE, whitePawns, 'e', false, false);
        whitePawns = movePawn(1, Color.WHITE, whitePawns, 'e', false, false);
        blackPawns = movePawn(2, Color.BLACK, blackPawns, 'd', false, false);

        testing(Color.WHITE, whitePawns | blackPawns);

    }

    private void testing(Color color, long pawns) {
        Position lastMoveDoubleStepPawnPosition = new Position('d', 5);


        int enPassantRank = (color == Color.WHITE) ? 5 : 2; // For white, the en passant rank is 6 (5 in 0-based index); for black, it's 3 (2 in 0-based index).
        int fileIndexOfDoubleSteppedPawn = lastMoveDoubleStepPawnPosition.getX() - 'a';
        int enPassantIndex = (enPassantRank * 8) + fileIndexOfDoubleSteppedPawn;
        long enPassantTargetSquare = 1L << enPassantIndex;

        // Calculate potential en passant capture moves to the left and right
        long attacksLeftEnPassant = (color == Color.WHITE) ? (pawns & ~FileMasks[7]) >>> 1 : (~FileMasks[7]) << 1;
        long attacksRightEnPassant = (color == Color.WHITE) ? (~FileMasks[0]) << 1 : (pawns & ~FileMasks[0]) >>> 1;

        //long attacksLeftEnPassant = 0x800003780L;
        //long attacksRightEnPassant = 0x200001dc00L;

        log.info("EnPassantTargetSquare before masking: {}", "0x" + Long.toHexString(enPassantTargetSquare));

        log.info("Attacks left before masking: {}", "0x" + Long.toHexString(attacksLeftEnPassant));
        log.info("Attacks right before masking: {}", "0x" + Long.toHexString(attacksRightEnPassant));

        // Filter out captures that don't have an opponent pawn in the en passant target square
        long validEnPassantCapturesLeft = attacksLeftEnPassant & enPassantTargetSquare;
        long validEnPassantCapturesRight = attacksRightEnPassant & enPassantTargetSquare;

        if (validEnPassantCapturesLeft != 0) {
            System.out.println("LEFT");
            printBitboard(validEnPassantCapturesLeft);
        }

        if (validEnPassantCapturesRight != 0) {
            System.out.println("RIGHT");
            printBitboard(validEnPassantCapturesRight);
        }
    }

    private long movePawn(int amountOfFields, Color color, long pawns, char file, boolean isCaptureToTheLeft, boolean isCaptureToTheRight) {
        int capture = 0;

        if (isCaptureToTheLeft && isCaptureToTheRight) {
            throw new IllegalStateException("Pawns cannot capture left and right!");
        }

        if (isCaptureToTheLeft) {
            capture = Color.WHITE == color ? -1 : 1;
        }

        if (isCaptureToTheRight) {
            capture = Color.WHITE == color ? 1 : -1;
        }

        return Color.WHITE == color ?
                (pawns & ~FileMasks[file - 'a']) | (pawns & FileMasks[file - 'a']) << 8 * amountOfFields + capture :
                (pawns & ~FileMasks[file - 'a']) | (pawns & FileMasks[file - 'a']) >> 8 * amountOfFields + capture;
    }

    private long removePawnFromPosition(Position position, long pawns) {
        // Create a mask that represents the position to remove
        long mask = FileMasks[position.getX() - 'a'] & RankMasks[position.getY() - 1];
        // Flip the bits of the mask to have 0 at the position to remove and 1s elsewhere
        mask = ~mask;
        // Use bitwise AND to clear the bit at the specified position
        return pawns & mask;
    }


    public void printBitboard(long bitboard) {
        for (int rank = 8; rank >= 1; rank--) {
            for (int file = 1; file <= 8; file++) {
                long mask = 1L << ((rank - 1) * 8 + (file - 1));
                if ((bitboard & mask) != 0) {
                    System.out.print("P ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}

