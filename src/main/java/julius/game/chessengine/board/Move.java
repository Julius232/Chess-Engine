package julius.game.chessengine.board;

import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;

public class Move {
    private final Position from;
    private final Position to;
    private final PieceType pieceType;
    private final Color color; // Color of the piece being moved
    private final boolean isCapture;
    private final boolean isCastlingMove;
    private final boolean isEnPassantMove;
    private final PieceType promotionPieceType; // This can be null if no promotion

    public Move(Position from, Position to, PieceType pieceType, Color color,
                boolean isCapture, boolean isCastlingMove, boolean isEnPassantMove, PieceType promotionPieceType) {
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.color = color;
        this.isCapture = isCapture || isEnPassantMove; // Capture is true if it's a regular capture or an en passant capture
        this.isCastlingMove = isCastlingMove;
        this.isEnPassantMove = isEnPassantMove;
        this.promotionPieceType = promotionPieceType;
    }

    // Getters for all the fields
    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public Color getColor() {
        return color;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public boolean isCastlingMove() {
        return isCastlingMove;
    }

    public boolean isEnPassantMove() {
        return isEnPassantMove;
    }

    public PieceType getPromotionPieceType() {
        return promotionPieceType;
    }

    // You may want to override toString() for easy move printing.
    @Override
    public String toString() {
        StringBuilder moveString = new StringBuilder(color.toString() + " " + pieceType + " from " + from + " to " + to);
        if (isCapture) {
            moveString.append(", capture");
        }
        if (isCastlingMove) {
            moveString.append(", castling");
        }
        if (isEnPassantMove) {
            moveString.append(", en passant");
        }
        if (promotionPieceType != null) {
            moveString.append(", promoting to ").append(promotionPieceType);
        }
        return moveString.toString();
    }

    // Additional methods as needed...
}
