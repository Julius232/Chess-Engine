package julius.game.chessengine.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class Score {
    private int scoreWhite;
    private int scoreBlack;

    public Score() {
        this.scoreWhite = 0;
        this.scoreBlack = 0;
    }

    public Score(int scoreWhite, int scoreBlack) {
        this.scoreWhite = scoreWhite;
        this.scoreBlack = scoreBlack;
    }

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
