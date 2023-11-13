package julius.game.chessengine.ai;

import julius.game.chessengine.board.Move;

class TranspositionTableEntry {
    double score;
    int depth;
    NodeType nodeType;
    Move bestMove; // Added to store the best move

    TranspositionTableEntry(double score, int depth, NodeType nodeType, Move bestMove) {
        this.score = score;
        this.depth = depth;
        this.nodeType = nodeType;
        this.bestMove = bestMove;
    }
}

enum NodeType {
    EXACT, // exact score
    LOWERBOUND, // failed high, value is a lower bound
    UPPERBOUND // failed low, value is an upper bound
}