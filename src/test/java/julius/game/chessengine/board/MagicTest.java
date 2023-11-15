package julius.game.chessengine.board;

import julius.game.chessengine.helper.BishopHelper;
import julius.game.chessengine.helper.RookHelper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.List;

@Log4j2
public class MagicTest {

    @Test
    public void testMagicMoves() {
        RookHelper rookHelper = new RookHelper();
        BishopHelper bishopHelper = new BishopHelper();

        // Initialize and find magic numbers
        log.info("Finding magic numbers...");
        rookHelper.findMagicNumbers();
        bishopHelper.findMagicNumbers();

        // Test a few squares to validate rook moves
        //testRookMovesForSquare(rookHelper, 0); // Example: test top-left corner of the board
        for(int i = 0; i<64; i++) {
            if(rookHelper.rookMagics[i] != 0) {
                testRookMovesForSquare(rookHelper, i);
            }
        }// Example: test bottom-right corner of the board
        for(int i = 0; i<64; i++) {
            if(bishopHelper.bishopMagics[i] != 0) {
                testBishopMovesForSquare(bishopHelper, i);
            }
        }
        // Add more tests for different squares as needed

        log.info(rookHelper.rookMagics);
    }

    private void testRookMovesForSquare(RookHelper rookHelper, int square) {
        long mask = rookHelper.generateOccupancyMask(square);
        List<Long> occupancies = rookHelper.generateAllOccupancies(mask);

        for (long occupancy : occupancies) {
            long moves = rookHelper.calculateRookMoves(square, occupancy);
            long magicMoves = calculateMovesUsingRookMagic(rookHelper, square, occupancy);

            // Log the results for comparison

            //log.info("Testing square: {}", square);
            //log.info("Occupancy: {}", occupancy);
            //log.info("Expected Moves: {}", moves);
            //log.info("Magic Moves: {}", magicMoves);

            // Assert that the moves match
            assert moves == magicMoves : "Move generation mismatch for square " + square;
        }
    }

    private long calculateMovesUsingRookMagic(RookHelper rookHelper, int square, long occupancy) {
        //log.info(" ------------------------------------- ");
        //log.info("rook mask for square [{}]", rookHelper.rookMasks[square]);
        // Calculate the index using the magic number
        int index = rookHelper.transform(occupancy, rookHelper.rookMagics[square], rookHelper.rookMasks[square]);
        // Retrieve the moves from the rookAttacks table
        return rookHelper.rookAttacks[square][index];
    }

    private void testBishopMovesForSquare(BishopHelper bishopHelper, int square) {
        long mask = bishopHelper.generateOccupancyMask(square);
        List<Long> occupancies = bishopHelper.generateAllOccupancies(mask);

        for (long occupancy : occupancies) {
            long moves = bishopHelper.calculateBishopMoves(square, occupancy);
            long magicMoves = calculateMovesUsingBishopMagic(bishopHelper, square, occupancy);

            // Log the results for comparison
            log.info("Testing square: {}", square);
            log.info("Occupancy: {}", Long.toBinaryString(occupancy));
            log.info("Expected Moves: {}", Long.toBinaryString(moves));
            log.info("Magic Moves: {}", Long.toBinaryString(magicMoves));

            // Assert that the moves match
        assert moves == magicMoves : "Move generation mismatch for square " + square;
    }
    }

    private long calculateMovesUsingBishopMagic(BishopHelper bishopHelper, int square, long occupancy) {
        // Calculate the index using the magic number
        int index = bishopHelper.transform(occupancy, bishopHelper.bishopMagics[square], bishopHelper.bishopMasks[square]);
        // Retrieve the moves from the rookAttacks table
        return bishopHelper.bishopAttacks[square][index];
    }

}
