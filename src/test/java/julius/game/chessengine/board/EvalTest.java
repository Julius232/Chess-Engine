package julius.game.chessengine.board;

import julius.game.chessengine.engine.Engine;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

@Log4j2
public class EvalTest {

    @Test
    public void quiencesearch() {
        Engine e = new Engine();
        e.importBoardFromFen("rnbqkbnr/ppppp2p/5pp1/7Q/4P3/8/PPPP1PPP/RNB1KBNR w KQkq - 0 3");
        log.info(e.evaluateBoard(true));
    }

}
