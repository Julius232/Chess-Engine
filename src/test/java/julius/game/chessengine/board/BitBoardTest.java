package julius.game.chessengine.board;

import static julius.game.chessengine.board.Position.convertStringToPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;

import julius.game.chessengine.engine.Engine;

import julius.game.chessengine.utils.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BitBoardTest {


    @Test
    public void testDoAndUndoTenMoves() {
        Engine engine = new Engine(); // The chess engine
        BitBoard board = engine.getBitBoard(); // The current state of the board from the engine

        // Lists to keep track of the moves and states
        List<Move> movesPerformed = new ArrayList<>();
        List<BitBoard> states = new ArrayList<>();

        // Do 10 moves
        for (int i = 0; i < 100; i++) {
            // Assume generateAllPossibleMoves and performMove are properly defined methods
            List<Move> possibleMoves = board.generateAllPossibleMoves(Color.WHITE);
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

    // Additional helper methods for the test if needed
}
