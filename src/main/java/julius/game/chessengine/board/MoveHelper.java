package julius.game.chessengine.board;

public class MoveHelper {

    public static boolean isWhitesMove(int move) {
        return (move & (1 << 15)) != 0;
    }

    public static int deriveLastMoveFromIndex(int move) {
        return move & 0x3F;

    }

    public static int deriveLastMovetoIndex(int move) {
        return  (move >> 6) & 0x3F;
    }

    public static int convertStringToIndex(String positionStr) {
        if (positionStr.length() != 2) {
            throw new IllegalArgumentException("Invalid position string: " + positionStr);
        }
        char fileChar = positionStr.charAt(0);
        int rank = Character.getNumericValue(positionStr.charAt(1));

        if (fileChar < 'a' || fileChar > 'h' || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position out of bounds: " + positionStr);
        }

        int file = fileChar - 'a'; // Convert file to 0-7 range
        rank = rank - 1; // Convert rank to 0-based index

        return rank * 8 + file;
    }

    public static String convertIndexToString(int index) {
        if (index < 0 || index >= 64) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        int rank = index / 8; // Determine the rank (0 to 7)
        int file = index % 8; // Determine the file (0 to 7)

        char fileChar = (char) ('a' + file); // Convert file to 'a' to 'h'
        int rankNumber = rank + 1; // Convert rank to 1-based index

        return fileChar + String.valueOf(rankNumber);
    }
}
