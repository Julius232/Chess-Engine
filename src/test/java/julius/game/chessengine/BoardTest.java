package julius.game.chessengine;

import org.junit.jupiter.api.Test;

public class BoardTest {

    @Test
    public void boardShouldReturnInitState() {

        Board board = new Board();

        assert board.isFigureOnField(new Position('a',2));



    }
}
