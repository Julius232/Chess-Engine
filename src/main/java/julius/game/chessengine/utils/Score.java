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

    public void add(int add, Color playerColor) {
        if (playerColor == Color.WHITE) {
            this.scoreWhite += add;
        } else if (playerColor == Color.BLACK) {
            this.scoreBlack += add;
        } else {
            log.error("Invalid player color");
        }
    }

    public int getScoreDifference(Color color) {
        return color.equals(Color.WHITE) ? getScoreWhite() - getScoreBlack() : getScoreBlack() - getScoreWhite();
    }
}
