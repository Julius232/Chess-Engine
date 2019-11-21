package julius.game.chessengine;

import lombok.Data;

@Data
public class Position {

    private char xAchse;

    private int yAchse;

    public Position(char xAchse, int yAchse) {
        this.xAchse = xAchse;
        this.yAchse = yAchse;
    }
}
