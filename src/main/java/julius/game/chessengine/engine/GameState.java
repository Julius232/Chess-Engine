package julius.game.chessengine.engine;

import lombok.Data;

@Data
public class GameState {

    private boolean whiteWon = false;
    private boolean blackWon = false;
    private boolean draw = false;

}
