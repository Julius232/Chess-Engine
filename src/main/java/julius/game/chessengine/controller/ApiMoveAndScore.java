package julius.game.chessengine.controller;

import lombok.Data;

@Data
public class ApiMoveAndScore {

    String move;
    double score;

    ApiMoveAndScore(String move, double score) {
        this.move = move;
        this.score = score;
    }

}