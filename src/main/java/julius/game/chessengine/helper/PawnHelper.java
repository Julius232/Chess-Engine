package julius.game.chessengine.helper;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static julius.game.chessengine.helper.BitHelper.bitIndex;
import static julius.game.chessengine.helper.BitHelper.fileBitboard;

@Log4j2
public class PawnHelper {

    public final static int[] WHITE_PAWN_POSITIONAL_VALUES = {
            0, 0, 0, 0, 0, 0, 0, 0, // Rank 1
            5, 5, 5, 10, 10, 5, 5, 5, // Rank 2
            10, 10, 10, 15, 15, 10, 10, 10, // Rank 3
            15, 15, 15, 20, 20, 15, 15, 15, // Rank 4
            20, 20, 20, 25, 25, 20, 20, 20, // Rank 5
            25, 25, 30, 35, 35, 30, 25, 25, // Rank 6
            30, 30, 35, 40, 40, 35, 30, 30, // Rank 7 -- close to promotion
            0, 0, 0, 0, 0, 0, 0, 0 // Rank 8
    };

    public final static int[] BLACK_PAWN_POSITIONAL_VALUES = {
            0, 0, 0, 0, 0, 0, 0, 0, // Rank 1
            30, 30, 35, 40, 40, 35, 30, 30,// Rank 2 -- close to promotion
            25, 25, 30, 35, 35, 30, 25, 25,// Rank 3
            20, 20, 20, 25, 25, 20, 20, 20, // Rank 4
            15, 15, 15, 20, 20, 15, 15, 15,  // Rank 5
            10, 10, 10, 15, 15, 10, 10, 10,// Rank 6
            5, 5, 5, 10, 10, 5, 5, 5, // Rank 7
            0, 0, 0, 0, 0, 0, 0, 0 // Rank 8
    };

    // Method to count pawns in the center (e4, d4, e5, d5 squares)
    public static int countCenterPawns(long pawnsBitboard) {
        // Bit positions for e4, d4, e5, d5
        long centerSquares = (1L << bitIndex('e', 4)) | (1L << bitIndex('d', 4))
                | (1L << bitIndex('e', 5)) | (1L << bitIndex('d', 5));
        return Long.bitCount(pawnsBitboard & centerSquares);
    }


    // Method to count doubled pawns, which are two pawns of the same color on the same file
    public static int countDoubledPawns(long pawnsBitboard) {
        int doubledPawns = 0;
        for (char file = 'a'; file <= 'h'; file++) {
            long fileBitboard = fileBitboard(file);
            if (Long.bitCount(pawnsBitboard & fileBitboard) > 1) {
                doubledPawns++;
            }
        }
        return doubledPawns;
    }

    // Method to count isolated pawns, which are pawns with no friendly pawns on adjacent files
    public static int countIsolatedPawns(long pawnsBitboard) {
        int isolatedPawns = 0;
        for (char file = 'a'; file <= 'h'; file++) {
            long fileBitboard = fileBitboard(file);
            long adjacentFiles = (file > 'a' ? fileBitboard((char) (file - 1)) : 0L)
                    | (file < 'h' ? fileBitboard((char) (file + 1)) : 0L);
            if ((pawnsBitboard & fileBitboard) != 0 && (pawnsBitboard & adjacentFiles) == 0) {
                isolatedPawns++;
            }
        }
        return isolatedPawns;
    }

    // Helper method to get a bitboard representing a file


}
