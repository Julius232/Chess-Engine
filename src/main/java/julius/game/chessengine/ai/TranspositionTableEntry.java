package julius.game.chessengine.ai;

class TranspositionTableEntry {
    double score;
    int depth;
    NodeType nodeType;

    public TranspositionTableEntry(double score, int depth, NodeType nodeType) {
        this.score = score;
        this.depth = depth;
        this.nodeType = nodeType;
    }
}

enum NodeType {
    EXACT, // exact score
    LOWERBOUND, // failed high, value is a lower bound
    UPPERBOUND // failed low, value is an upper bound
}