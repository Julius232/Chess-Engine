package julius.game.chessengine.ai;

class TranspositionTableEntry {
    double score;
    int depth;
    boolean isExact;

    TranspositionTableEntry(double score, int depth, boolean isExact) {
        this.score = score;
        this.depth = depth;
        this.isExact = isExact;
    }
}