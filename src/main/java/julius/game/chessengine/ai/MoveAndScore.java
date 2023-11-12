package julius.game.chessengine.ai;


import julius.game.chessengine.board.Move;

class MoveAndScore {
    Move move;
    double score;

    MoveAndScore(Move move, double score) {
        this.move = move;
        this.score = score;
    }
}