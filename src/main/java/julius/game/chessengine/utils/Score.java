package julius.game.chessengine.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class Score {
    private int scoreWhite;
    private int scoreBlack;

    public Score(int scoreWhite, int scoreBlack) {
        this.scoreWhite = scoreWhite;
        this.scoreBlack = scoreBlack;
    }

    public int getScoreDifference() {
        return (getScoreWhite() - getScoreBlack());
    }
}
