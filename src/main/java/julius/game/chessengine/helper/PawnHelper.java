package julius.game.chessengine.helper;

import julius.game.chessengine.board.Move;
import julius.game.chessengine.utils.Color;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static julius.game.chessengine.helper.BitHelper.bitIndex;

@Log4j2
public class PawnHelper {

    public final static int[] WHITE_PAWN_POSITIONAL_VALUES = {
            // Rank 1 - Base rank for white, no pawns should be here
            0, 0, 0, 0, 0, 0, 0, 0,
            // Rank 2 - Initial rank for white pawns
            5, 10, 10, -10, -10, 10, 10, 5,
            // Rank 3 - Pawns have moved one square
            5, -5, -10, 0, 0, -10, -5, 5,
            // Rank 4 - Pawns have moved two squares
            10, 0, 0, 20, 20, 0, 0, 10,
            // Rank 5 - Pawns are advancing and potentially supporting attack
            15, 10, 10, 25, 25, 10, 10, 15,
            // Rank 6 - Pawns are far advanced and can become very dangerous
            20, 20, 20, 30, 30, 20, 20, 20,
            // Rank 7 - Pawns are very close to promotion
            25, 25, 25, 40, 40, 25, 25, 25,
            // Rank 8 - Pawns should never be here (promotion should have occurred)
            0, 0, 0, 0, 0, 0, 0, 0
    };

    public final static int[] BLACK_PAWN_POSITIONAL_VALUES = {
            // Rank 1 - Base rank for white, no pawns should be here
            0, 0, 0, 0, 0, 0, 0, 0,
            // Rank 2 - Initial rank for white pawns
            25, 25, 25, 40, 40, 25, 25, 25,
            // Rank 3 - Pawns have moved one square
            20, 20, 20, 30, 30, 20, 20, 20,
            // Rank 4 - Pawns have moved two squares
            15, 10, 10, 25, 25, 10, 10, 15,
            // Rank 5 - Pawns are advancing and potentially supporting attack
            10, 0, 0, 20, 20, 0, 0, 10,
            // Rank 6 - Pawns are far advanced and can become very dangerous
            5, -5, -10, 0, 0, -10, -5, 5,
            // Rank 7 - Pawns are very close to promotion
            5, 10, 10, -10, -10, 10, 10, 5,
            // Rank 8 - Pawns should never be here (promotion should have occurred)
            0, 0, 0, 0, 0, 0, 0, 0
    };

    public static int countCenterPawns(long pawnsBitboard) {
        // Bit positions for e4, d4, e5, d5
        long centerSquares = (1L << bitIndex('e', 4)) | (1L << bitIndex('d', 4))
                | (1L << bitIndex('e', 5)) | (1L << bitIndex('d', 5));
        return Long.bitCount(pawnsBitboard & centerSquares);
    }

}
