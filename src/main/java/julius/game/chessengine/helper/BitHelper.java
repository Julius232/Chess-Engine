package julius.game.chessengine.helper;

import julius.game.chessengine.board.Position;

public class BitHelper {

    public static final long[] RankMasks = new long[]{
            0x00000000000000FFL, // Rank 1
            0x000000000000FF00L, // Rank 2
            0x0000000000FF0000L, // Rank 3
            0x00000000FF000000L, // Rank 4
            0x000000FF00000000L, // Rank 5
            0x0000FF0000000000L, // Rank 6
            0x00FF000000000000L, // Rank 7
            0xFF00000000000000L  // Rank 8
    };

    public static final long[] FileMasks = new long[]{
            0x0101010101010101L, // File A
            0x0202020202020202L, // File B
            0x0404040404040404L, // File C
            0x0808080808080808L, // File D
            0x1010101010101010L, // File E
            0x2020202020202020L, // File F
            0x4040404040404040L, // File G
            0x8080808080808080L  // File H
    };

    private static final boolean[][] validPositions = {
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true}
    };

    public static boolean isValidBoardPosition(Position position) {
        int fileIndex = position.getX() - 'a';
        int rankIndex = position.getY() - 1;
        return fileIndex >= 0 && fileIndex < 8 && rankIndex >= 0 && rankIndex < 8
                && validPositions[fileIndex][rankIndex];
    }



    public static int bitIndex(char file, int rank) {
        return (rank - 1) * 8 + (file - 'a');
    }

}
