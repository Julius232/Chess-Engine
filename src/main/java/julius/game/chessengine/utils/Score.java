package julius.game.chessengine.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class Score {
    private int scoreWhite = 0;
    private int scoreBlack = 0;

    public void add(int add, String playerColor) {
        if(playerColor.equals(Color.WHITE)) {
            scoreWhite += add;
            log.info("White's score: " + scoreWhite);
        }
        else {
            scoreBlack += add;
            log.info("Black's score: " + scoreBlack);
        }
    }
}
