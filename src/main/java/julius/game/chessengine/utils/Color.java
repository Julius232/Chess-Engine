package julius.game.chessengine.utils;

public class Color {
    public static final String WHITE = "white";
    public static final String BLACK = "black";

    public static String getOpponentColor(String color) {
        return color.equals(WHITE) ? BLACK : WHITE;
    }
}
