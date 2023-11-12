package julius.game.chessengine.helper;

public class KnightHelper {

    // The possible moves for a knight from its current position
    public static final int[][] knightMoves = {
            {-2, -1}, {-2, 1}, // Upwards L-moves
            {2, -1}, {2, 1},   // Downwards L-moves
            {-1, -2}, {1, -2}, // Leftwards L-moves
            {-1, 2}, {1, 2}    // Rightwards L-moves
    };

    public static final int[] KNIGHT_POSITIONAL_VALUES = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,  10,  10,  10,  10,   5, -10,
            -10,   5,   0,   0,   0,   0,   5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
    };
}
