package julius.game.chessengine.board;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class BitBoard {
    private long whitePawns = 0L;
    private long blackPawns = 0L;
    private long whiteKnights = 0L;
    private long blackKnights = 0L;
    private long whiteBishops = 0L;
    private long blackBishops = 0L;
    private long whiteRooks = 0L;
    private long blackRooks = 0L;
    private long whiteQueens = 0L;
    private long blackQueens = 0L;
    private long whiteKing = 0L;
    private long blackKing = 0L;
    private long whitePieces = 0L;
    private long blackPieces = 0L;
    private long allPieces = 0L;

    // This variable needs to be set whenever a move is made
    private Position lastMoveDoubleStepPawnPosition;

    // Flags to track if the king and rooks have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookA1Moved = false;
    private boolean whiteRookH1Moved = false;
    private boolean blackRookA8Moved = false;
    private boolean blackRookH8Moved = false;

    public BitBoard(BitBoard other) {
        // Copying all the long fields representing the pieces
        this.whitePawns = other.whitePawns;
        this.blackPawns = other.blackPawns;
        this.whiteKnights = other.whiteKnights;
        this.blackKnights = other.blackKnights;
        this.whiteBishops = other.whiteBishops;
        this.blackBishops = other.blackBishops;
        this.whiteRooks = other.whiteRooks;
        this.blackRooks = other.blackRooks;
        this.whiteQueens = other.whiteQueens;
        this.blackQueens = other.blackQueens;
        this.whiteKing = other.whiteKing;
        this.blackKing = other.blackKing;

        // Copying the combined bitboards
        this.whitePieces = other.whitePieces;
        this.blackPieces = other.blackPieces;
        this.allPieces = other.allPieces;

        // Copying the flags
        this.whiteKingMoved = other.whiteKingMoved;
        this.blackKingMoved = other.blackKingMoved;
        this.whiteRookA1Moved = other.whiteRookA1Moved;
        this.whiteRookH1Moved = other.whiteRookH1Moved;
        this.blackRookA8Moved = other.blackRookA8Moved;
        this.blackRookH8Moved = other.blackRookH8Moved;

        this.lastMoveDoubleStepPawnPosition = other.lastMoveDoubleStepPawnPosition != null ? new Position(other.lastMoveDoubleStepPawnPosition) : null;
    }


    // ... other members and methods ...

    // Call these methods within the movePiece method when a king or rook moves
    private void markKingAsMoved(Color color) {
        if (color == Color.WHITE) {
            whiteKingMoved = true;
        } else {
            blackKingMoved = true;
        }
    }

    private void markRookAsMoved(Position rookPosition) {
        if (rookPosition.equals(new Position('a', 1))) {
            whiteRookA1Moved = true;
        } else if (rookPosition.equals(new Position('h', 1))) {
            whiteRookH1Moved = true;
        } else if (rookPosition.equals(new Position('a', 8))) {
            blackRookA8Moved = true;
        } else if (rookPosition.equals(new Position('h', 8))) {
            blackRookH8Moved = true;
        }
    }

    // Constructor to initialize the board to the starting position
    public BitBoard() {
        setInitialPosition();
    }

    // Method to set up the initial position
    public void setInitialPosition() {
        // Setting white pawns on the second rank
        whitePawns = 0x000000000000FF00L;
        // Setting black pawns on the seventh rank
        blackPawns = 0x00FF000000000000L;

        // Setting white knights on b1 and g1
        whiteKnights = (1L << bitIndex('b', 1)) | (1L << bitIndex('g', 1));
        // Setting black knights on b8 and g8
        blackKnights = (1L << bitIndex('b', 8)) | (1L << bitIndex('g', 8));

        // Setting white bishops on c1 and f1
        whiteBishops = (1L << bitIndex('c', 1)) | (1L << bitIndex('f', 1));
        // Setting black bishops on c8 and f8
        blackBishops = (1L << bitIndex('c', 8)) | (1L << bitIndex('f', 8));

        // Setting white rooks on a1 and h1
        whiteRooks = (1L << bitIndex('a', 1)) | (1L << bitIndex('h', 1));
        // Setting black rooks on a8 and h8
        blackRooks = (1L << bitIndex('a', 8)) | (1L << bitIndex('h', 8));

        // Setting white queen on d1
        whiteQueens = 1L << bitIndex('d', 1);
        // Setting black queen on d8
        blackQueens = 1L << bitIndex('d', 8);

        // Setting white king on e1
        whiteKing = 1L << bitIndex('e', 1);
        // Setting black king on e8
        blackKing = 1L << bitIndex('e', 8);

        // Setting all white pieces by combining the bitboards
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        // Setting all black pieces by combining the bitboards
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;

        // Setting all pieces on the board
        allPieces = whitePieces | blackPieces;
    }


    // Method to calculate the bit index based on position
    public int bitIndex(char file, int rank) {
        return (rank - 1) * 8 + (file - 'a');
    }

    // Method to move a piece
    public void movePiece(PieceType piece, Color color, Position from, Position to) {
        // Translate positions to bitboard indices
        int fromIndex = bitIndex(from.getX(), from.getY());
        int toIndex = bitIndex(to.getX(), to.getY());

        // Find the correct bitboard based on the piece type and color
        long pieceBitboard = getBitboardForPiece(piece, color);

        // Check if there is a piece at the destination to capture
        if (isOccupied(to)) {
            // Get the color of the piece at the destination
            Color opponentColor = getPieceColorAtPosition(to) == Color.WHITE ? Color.BLACK : Color.WHITE;
            // If there is an opponent's piece, remove it from its bitboard
            clearSquare(toIndex, opponentColor);
        }

        // Move the piece
        pieceBitboard = moveBit(pieceBitboard, fromIndex, toIndex);

        // Update the bitboard for the moved piece
        setBitboardForPiece(piece, color, pieceBitboard);

        // If the move is a king or rook move, we must mark them as moved for castling rights
        if (piece == PieceType.KING) {
            markKingAsMoved(color);
        } else if (piece == PieceType.ROOK) {
            markRookAsMoved(from);
        }
    }

    // Helper method to move a piece on a bitboard
    private long moveBit(long pieceBitboard, int fromIndex, int toIndex) {
        // Clear the bit at the from index
        pieceBitboard &= ~(1L << fromIndex);
        // Set the bit at the to index
        pieceBitboard |= 1L << toIndex;
        return pieceBitboard;
    }

    // Method to get the bitboard for a specific piece type and color
    public long getBitboardForPiece(PieceType piece, Color color) {
        return switch (color) {
            case WHITE -> switch (piece) {
                case PAWN -> whitePawns;
                case KNIGHT -> whiteKnights;
                case BISHOP -> whiteBishops;
                case ROOK -> whiteRooks;
                case QUEEN -> whiteQueens;
                case KING -> whiteKing;
            };
            case BLACK -> switch (piece) {
                case PAWN -> blackPawns;
                case KNIGHT -> blackKnights;
                case BISHOP -> blackBishops;
                case ROOK -> blackRooks;
                case QUEEN -> blackQueens;
                case KING -> blackKing;
            };
        };
    }


    // Method to set the bitboard for a specific piece type and color
    private void setBitboardForPiece(PieceType piece, Color color, long bitboard) {
        switch (color) {
            case WHITE:
                switch (piece) {
                    case PAWN -> whitePawns = bitboard;
                    case KNIGHT -> whiteKnights = bitboard;
                    case BISHOP -> whiteBishops = bitboard;
                    case ROOK -> whiteRooks = bitboard;
                    case QUEEN -> whiteQueens = bitboard;
                    case KING -> whiteKing = bitboard;
                    default -> throw new IllegalArgumentException("Unknown piece type: " + piece);
                }
                break;
            case BLACK:
                switch (piece) {
                    case PAWN -> blackPawns = bitboard;
                    case KNIGHT -> blackKnights = bitboard;
                    case BISHOP -> blackBishops = bitboard;
                    case ROOK -> blackRooks = bitboard;
                    case QUEEN -> blackQueens = bitboard;
                    case KING -> blackKing = bitboard;
                    default -> throw new IllegalArgumentException("Unknown piece type: " + piece);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown color: " + color);
        }
        // After setting the bitboard, update the aggregated bitboards
        updateAggregatedBitboards();
    }

    private void updateAggregatedBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }

    public List<Move> generateAllPossibleMoves(Color color) {
        List<Move> moves = new ArrayList<>();

        // Generate moves for each piece type
        moves.addAll(generatePawnMoves(color));
        moves.addAll(generateKnightMoves(color));
        moves.addAll(generateBishopMoves(color));
        moves.addAll(generateRookMoves(color));
        moves.addAll(generateQueenMoves(color));
        moves.addAll(generateKingMoves(color));

        return moves;
    }

    private List<Move> generatePawnMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
        long opponentPieces = (color == Color.WHITE) ? blackPieces : whitePieces;
        long emptySquares = ~(whitePieces | blackPieces);
        int direction = (color == Color.WHITE) ? 8 : -8; // Up for white, down for black
        int promotionRank = (color == Color.WHITE) ? 7 : 0;
        long rankMask = (color == Color.WHITE) ? 0x00FF000000000000L : 0x0000000000FF0000L;

        // Generate moves for each pawn
        for (int i = Long.numberOfTrailingZeros(pawns); i < 64 - Long.numberOfLeadingZeros(pawns); i++) {
            if (((1L << i) & pawns) != 0) {
                int rank = i / 8;
                int file = i % 8;

                // Single move forward
                if ((1L << (i + direction) & emptySquares) != 0) {
                    moves.add(new Move(indexToPosition(i), indexToPosition(i + direction), PieceType.PAWN, color, false, false, false, (rank == promotionRank) ? PieceType.QUEEN : null));
                    // Initial double move
                    if (((1L << i) & rankMask) != 0) {
                        moves.add(new Move(indexToPosition(i), indexToPosition(i + 2 * direction), PieceType.PAWN, color, false, false, false, null));
                    }
                }

                // Captures
                if (file > 0 && (1L << (i + direction - 1) & opponentPieces) != 0) { // Capture left
                    moves.add(new Move(indexToPosition(i), indexToPosition(i + direction - 1), PieceType.PAWN, color, true, false, false, (rank == promotionRank) ? PieceType.QUEEN : null));
                }
                if (file < 7 && (1L << (i + direction + 1) & opponentPieces) != 0) { // Capture right
                    moves.add(new Move(indexToPosition(i), indexToPosition(i + direction + 1), PieceType.PAWN, color, true, false, false, (rank == promotionRank) ? PieceType.QUEEN : null));
                }
            }
        }

        return moves;
    }


    private Move createMove(int fromIndex, int toIndex, Color color, boolean isCapture, boolean isPromotion) {
        Position from = indexToPosition(fromIndex);
        Position to = indexToPosition(toIndex);
        PieceType promotionPieceType = isPromotion ? PieceType.QUEEN : null; // Assume promotion to queen for simplicity
        return new Move(from, to, PieceType.PAWN, color, isCapture, false, (to == lastMoveDoubleStepPawnPosition), promotionPieceType);
    }


    private List<Move> generateKnightMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long knights = (color == Color.WHITE) ? whiteKnights : blackKnights;
        long ownPieces = (color == Color.WHITE) ? whitePieces : blackPieces;

        // The possible moves for a knight from its current position
        int[][] knightOffsets = {
                {-2, -1}, {-2, 1}, // Upwards L-moves
                {2, -1}, {2, 1},   // Downwards L-moves
                {-1, -2}, {1, -2}, // Leftwards L-moves
                {-1, 2}, {1, 2}    // Rightwards L-moves
        };

        // Iterate over all bits where knights exist
        for (int i = Long.numberOfTrailingZeros(knights); i < 64 - Long.numberOfLeadingZeros(knights); i++) {
            if (((1L << i) & knights) != 0) {
                int fromRank = i / 8;
                int fromFile = i % 8;
                Position fromPosition = indexToPosition(i);

                // Iterate through all possible L moves
                for (int[] offset : knightOffsets) {
                    int toFile = fromFile + offset[0];
                    int toRank = fromRank + offset[1];

                    // Check if the target position is within the board limits
                    if (toFile >= 0 && toFile < 8 && toRank >= 0 && toRank < 8) {
                        int toIndex = toRank * 8 + toFile;
                        Position toPosition = indexToPosition(toIndex);

                        // Check if the target position is occupied by own piece
                        if ((ownPieces & (1L << toIndex)) == 0) {
                            // Determine if the target position is a capture
                            boolean isCapture = (allPieces & (1L << toIndex)) != 0;
                            // Add move to the list (no promotion for knights, hence null)
                            moves.add(new Move(fromPosition, toPosition, PieceType.KNIGHT, color, isCapture, false, false, null));
                        }
                    }
                }
            }
        }

        return moves;
    }


    private List<Move> generateBishopMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long bishops = (color == Color.WHITE) ? whiteBishops : blackBishops;
        long ownPieces = (color == Color.WHITE) ? whitePieces : blackPieces;
        long opponentPieces = (color == Color.WHITE) ? blackPieces : whitePieces;

        // Directions a bishop can move: top-left, top-right, bottom-left, bottom-right
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        // Iterate over all bits where bishops exist
        for (int i = Long.numberOfTrailingZeros(bishops); i < 64 - Long.numberOfLeadingZeros(bishops); i++) {
            if (((1L << i) & bishops) != 0) {
                Position fromPosition = indexToPosition(i);

                // Iterate through each direction a bishop can move
                for (int[] direction : directions) {
                    int toFile = fromPosition.getX() - 'a';
                    int toRank = fromPosition.getY() - 1;

                    // Keep moving in the direction until you hit the edge of the board or another piece
                    while (true) {
                        toFile += direction[0];
                        toRank += direction[1];

                        if (toFile >= 0 && toFile < 8 && toRank >= 0 && toRank < 8) {
                            int toIndex = toRank * 8 + toFile;
                            Position toPosition = indexToPosition(toIndex);

                            // If the position is occupied by own piece, break the loop, can't jump over
                            if ((ownPieces & (1L << toIndex)) != 0) {
                                break;
                            }

                            // If it's an opponent's piece, it's a capture move
                            boolean isCapture = (opponentPieces & (1L << toIndex)) != 0;
                            moves.add(new Move(fromPosition, toPosition, PieceType.BISHOP, color, isCapture, false, false, null));

                            // If you capture a piece, you must stop.
                            if (isCapture) {
                                break;
                            }
                        } else {
                            // If the position is off the board, stop checking this direction
                            break;
                        }
                    }
                }
            }
        }

        return moves;
    }


    private List<Move> generateRookMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long rooks = (color == Color.WHITE) ? whiteRooks : blackRooks;
        long ownPieces = (color == Color.WHITE) ? whitePieces : blackPieces;
        long opponentPieces = (color == Color.WHITE) ? blackPieces : whitePieces;

        // Directions a rook can move: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Iterate over all bits where rooks exist
        for (int i = Long.numberOfTrailingZeros(rooks); i < 64 - Long.numberOfLeadingZeros(rooks); i++) {
            if (((1L << i) & rooks) != 0) {
                Position fromPosition = indexToPosition(i);

                // Iterate through each direction a rook can move
                for (int[] direction : directions) {
                    int toFile = fromPosition.getX() - 'a';
                    int toRank = fromPosition.getY() - 1;

                    // Keep moving in the direction until you hit the edge of the board or another piece
                    while (true) {
                        toFile += direction[0];
                        toRank += direction[1];

                        if (toFile >= 0 && toFile < 8 && toRank >= 0 && toRank < 8) {
                            int toIndex = toRank * 8 + toFile;
                            Position toPosition = indexToPosition(toIndex);

                            // If the position is occupied by own piece, break the loop, can't jump over
                            if ((ownPieces & (1L << toIndex)) != 0) {
                                break;
                            }

                            // If it's an opponent's piece, it's a capture move
                            boolean isCapture = (opponentPieces & (1L << toIndex)) != 0;
                            moves.add(new Move(fromPosition, toPosition, PieceType.ROOK, color, isCapture, false, false, null));

                            // If you capture a piece, you must stop.
                            if (isCapture) {
                                break;
                            }
                        } else {
                            // If the position is off the board, stop checking this direction
                            break;
                        }
                    }
                }
            }
        }

        return moves;
    }


    private List<Move> generateQueenMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long queens = (color == Color.WHITE) ? whiteQueens : blackQueens;
        long ownPieces = (color == Color.WHITE) ? whitePieces : blackPieces;
        long opponentPieces = (color == Color.WHITE) ? blackPieces : whitePieces;

        // Directions a queen can move: horizontally, vertically, and diagonally
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // Horizontal and vertical
                {-1, -1}, {1, 1}, {-1, 1}, {1, -1}  // Diagonal
        };

        // Iterate over all bits where queens exist
        for (int i = Long.numberOfTrailingZeros(queens); i < 64 - Long.numberOfLeadingZeros(queens); i++) {
            if (((1L << i) & queens) != 0) {
                Position fromPosition = indexToPosition(i);

                // Iterate through each direction a queen can move
                for (int[] direction : directions) {
                    int toFile = fromPosition.getX() - 'a';
                    int toRank = fromPosition.getY() - 1;

                    // Keep moving in the direction until you hit the edge of the board or another piece
                    while (true) {
                        toFile += direction[0];
                        toRank += direction[1];

                        if (toFile >= 0 && toFile < 8 && toRank >= 0 && toRank < 8) {
                            int toIndex = toRank * 8 + toFile;
                            Position toPosition = indexToPosition(toIndex);

                            // If the position is occupied by own piece, break the loop, can't jump over
                            if ((ownPieces & (1L << toIndex)) != 0) {
                                break;
                            }

                            // If it's an opponent's piece, it's a capture move
                            boolean isCapture = (opponentPieces & (1L << toIndex)) != 0;
                            moves.add(new Move(fromPosition, toPosition, PieceType.QUEEN, color, isCapture, false, false, null));

                            // If you capture a piece, you must stop.
                            if (isCapture) {
                                break;
                            }
                        } else {
                            // If the position is off the board, stop checking this direction
                            break;
                        }
                    }
                }
            }
        }

        return moves;
    }

    private List<Move> generateKingMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long kingBitboard = (color == Color.WHITE) ? whiteKing : blackKing;
        int kingPositionIndex = Long.numberOfTrailingZeros(kingBitboard);
        Position kingPosition = indexToPosition(kingPositionIndex);

        // Offsets for a king's normal moves
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};

        for (int offset : offsets) {
            int targetIndex = kingPositionIndex + offset;
            // Check if the move is within board bounds and does not wrap around
            if (targetIndex >= 0 && targetIndex < 64 && !doesMoveWrapAround(kingPositionIndex, targetIndex)) {
                Position targetPosition = indexToPosition(targetIndex);
                if (!isOccupiedByColor(targetPosition, color)) {
                    boolean isCapture = isOccupiedByOpponent(targetPosition, color);
                    moves.add(new Move(kingPosition, targetPosition, PieceType.KING, color, isCapture, false, false, null));
                }
            }
        }

        // Castling logic
        // The actual castling checks should be implemented in the canCastleKingside and canCastleQueenside methods
        if (canKingCastle(color)) {
            // Kingside castling
            if (canCastleKingside(color, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex + 2), PieceType.KING, color, false, true, false, null));
            }
            // Queenside castling
            if (canCastleQueenside(color, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex - 2), PieceType.KING, color, false, true, false, null));
            }
        }

        return moves;
    }

    private boolean doesMoveWrapAround(int fromIndex, int toIndex) {
        int fromRank = fromIndex / 8;
        int fromFile = fromIndex % 8;
        int toRank = toIndex / 8;
        int toFile = toIndex % 8;
        // Check if the move wraps around the board horizontally or is outside the board vertically
        return Math.abs(fromFile - toFile) > 1 || toRank < 0 || toRank > 7;
    }


    private boolean isMoveWithinBoard(int fromIndex, int toIndex) {
        // Check if the move doesn't wrap around the edges of the board
        return Math.abs(toIndex % 8 - fromIndex % 8) <= 1;
    }

    private boolean canKingCastle(Color color) {
        // The king must not have moved and must not be in check
        return !hasKingMoved(color) && !isInCheck(color);
    }

    private boolean canCastleKingside(Color color, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] kingsideSquares = {kingPositionIndex + 1, kingPositionIndex + 2};
        for (int square : kingsideSquares) {
            if (isOccupied(indexToPosition(square)) || isSquareUnderAttack(indexToPosition(square), color)) {
                return false;
            }
        }
        return !hasRookMoved(new Position('h', color == Color.WHITE ? 1 : 8));
    }

    private boolean canCastleQueenside(Color color, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] queensideSquares = {kingPositionIndex - 1, kingPositionIndex - 2, kingPositionIndex - 3};
        for (int square : queensideSquares) {
            if (isOccupied(indexToPosition(square)) || isSquareUnderAttack(indexToPosition(square), color)) {
                return false;
            }
        }
        return !hasRookMoved(new Position('a', color == Color.WHITE ? 1 : 8));
    }

    private boolean isSquareUnderAttack(Position position, Color kingColor) {
        Color opponentColor = getOpponentColor(kingColor);
        return canPawnAttackPosition(position, opponentColor) ||
                canKnightAttackPosition(position, opponentColor) ||
                canBishopAttackPosition(position, opponentColor) ||
                canRookAttackPosition(position, opponentColor) ||
                canQueenAttackPosition(position, opponentColor) ||
                canKingAttackPosition(position, opponentColor);
    }

    private boolean canPawnAttackPosition(Position position, Color color) {
        // Pawns attack diagonally, so check the two squares from which an opponent pawn could attack
        int direction = (color == Color.WHITE) ? -1 : 1; // Pawns move up if white, down if black
        Position attackFromLeft = new Position((char) (position.getX() - 1), position.getY() + direction);
        Position attackFromRight = new Position((char) (position.getX() + 1), position.getY() + direction);
        return (isValidBoardPosition(attackFromLeft) && isOccupiedByPawn(attackFromLeft, color)) ||
                (isValidBoardPosition(attackFromRight) && isOccupiedByPawn(attackFromRight, color));
    }

// Similar methods would be implemented for knights, bishops, rooks, queens, and kings.
// These methods would use the attack patterns for each piece type to determine if they
// could possibly attack the given square. For example, a knight's attack pattern is an L-shape,
// a bishop attacks along diagonals, etc.

    // Example for knights:
    private boolean canKnightAttackPosition(Position position, Color color) {
        long knightsBitboard = (color == Color.WHITE) ? whiteKnights : blackKnights;
        long knightAttacks = knightAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (knightAttacks & knightsBitboard) != 0;
    }

// The knightAttackBitmask method would generate all the potential squares a knight can attack from a given position.
// It would look similar to the previously provided knightAttackBitmask method but would center around the target position instead.

    private boolean canBishopAttackPosition(Position position, Color color) {
        long bishopsBitboard = (color == Color.WHITE) ? whiteBishops : blackBishops;
        long bishopAttacks = bishopAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (bishopAttacks & bishopsBitboard) != 0;
    }

    private boolean canRookAttackPosition(Position position, Color color) {
        long rooksBitboard = (color == Color.WHITE) ? whiteRooks : blackRooks;
        long rookAttacks = rookAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (rookAttacks & rooksBitboard) != 0;
    }

    private boolean canQueenAttackPosition(Position position, Color color) {
        long queensBitboard = (color == Color.WHITE) ? whiteQueens : blackQueens;
        long queenAttacks = queenAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (queenAttacks & queensBitboard) != 0;
    }

    private boolean canKingAttackPosition(Position position, Color color) {
        long kingsBitboard = (color == Color.WHITE) ? whiteKing : blackKing;
        long kingAttacks = kingAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (kingAttacks & kingsBitboard) != 0;
    }

// The above-mentioned bishopAttackBitmask, rookAttackBitmask, queenAttackBitmask, and kingAttackBitmask methods
// would generate all potential squares each piece can attack from a given position and compare it to the current
// bitboard of that piece type to see if there's an overlap.

    public void performMove(Move move) {
        int fromIndex = bitIndex(move.getFrom().getX(), move.getFrom().getY());
        int toIndex = bitIndex(move.getTo().getX(), move.getTo().getY());

        long pieceBitboard = getBitboardForPiece(move.getPieceType(), move.getColor());

        if (move.isCapture()) {
            clearSquare(toIndex, getOpponentColor(move.getColor()));
        }

        // If the move is a castling move, move both the king and the rook
        if (move.isCastlingMove()) {
            // Determine if this is kingside or queenside castling
            boolean kingside = move.getTo().getX() > move.getFrom().getX();
            Position rookFromPosition, rookToPosition;
            if (kingside) {
                rookFromPosition = new Position('h', move.getFrom().getY());
                rookToPosition = new Position((char) (move.getTo().getX() - 1), move.getTo().getY());
            } else {
                rookFromPosition = new Position('a', move.getFrom().getY());
                rookToPosition = new Position((char) (move.getTo().getX() + 1), move.getTo().getY());
            }
            // Move the rook
            int rookFromIndex = bitIndex(rookFromPosition.getX(), rookFromPosition.getY());
            int rookToIndex = bitIndex(rookToPosition.getX(), rookToPosition.getY());
            long rookBitboard = getBitboardForPiece(PieceType.ROOK, move.getColor());
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(PieceType.ROOK, move.getColor(), rookBitboard);

            // Mark the rook as moved
            markRookAsMoved(rookFromPosition);
        }

        // Move the piece
        pieceBitboard = moveBit(pieceBitboard, fromIndex, toIndex);
        setBitboardForPiece(move.getPieceType(), move.getColor(), pieceBitboard);

        // Mark the king as moved if it was a king move
        if (move.getPieceType() == PieceType.KING) {
            markKingAsMoved(move.getColor());
        }

        if (move.getPieceType() == PieceType.PAWN && Math.abs(move.getFrom().getY() - move.getTo().getY()) == 2) {
            lastMoveDoubleStepPawnPosition = move.getTo();
        } else {
            lastMoveDoubleStepPawnPosition = null;
        }

        updateAggregatedBitboards();
    }


    public void clearSquare(int index, Color color) {
        long mask = ~(1L << index);
        if (color == Color.WHITE) {
            if ((whitePawns & mask) != 0L) whitePawns &= mask;
            if ((whiteKnights & mask) != 0L) whiteKnights &= mask;
            if ((whiteBishops & mask) != 0L) whiteBishops &= mask;
            if ((whiteRooks & mask) != 0L) whiteRooks &= mask;
            if ((whiteQueens & mask) != 0L) whiteQueens &= mask;  // This line should clear the queen
            whiteKing &= mask;
        } else {
            if ((blackPawns & mask) != 0L) blackPawns &= mask;
            if ((blackKnights & mask) != 0L) blackKnights &= mask;
            if ((blackBishops & mask) != 0L) blackBishops &= mask;
            if ((blackRooks & mask) != 0L) blackRooks &= mask;
            if ((blackQueens & mask) != 0L) blackQueens &= mask;  // This line should clear the queen
            blackKing &= mask;
        }
        updateAggregatedBitboards();
    }


    public PieceType getPieceTypeAtPosition(Position position) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;

        if ((whitePawns & positionMask) != 0) return PieceType.PAWN;
        if ((blackPawns & positionMask) != 0) return PieceType.PAWN;
        if ((whiteKnights & positionMask) != 0) return PieceType.KNIGHT;
        if ((blackKnights & positionMask) != 0) return PieceType.KNIGHT;
        if ((whiteBishops & positionMask) != 0) return PieceType.BISHOP;
        if ((blackBishops & positionMask) != 0) return PieceType.BISHOP;
        if ((whiteRooks & positionMask) != 0) return PieceType.ROOK;
        if ((blackRooks & positionMask) != 0) return PieceType.ROOK;
        if ((whiteQueens & positionMask) != 0) return PieceType.QUEEN;
        if ((blackQueens & positionMask) != 0) return PieceType.QUEEN;
        if ((whiteKing & positionMask) != 0) return PieceType.KING;
        if ((blackKing & positionMask) != 0) return PieceType.KING;

        // No piece found at this position
        return null;
    }

    public Color getPieceColorAtPosition(Position position) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;

        // Check if the position is occupied by a white piece
        if ((whitePieces & positionMask) != 0) {
            return Color.WHITE;
        }
        // Check if the position is occupied by a black piece
        else if ((blackPieces & positionMask) != 0) {
            return Color.BLACK;
        }

        // No piece found at this position
        return null;
    }

    public boolean isOccupied(Position position) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;
        return (allPieces & positionMask) != 0;
    }

    public boolean isOccupiedByOpponent(Position position, Color color) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;

        if (color == Color.WHITE) {
            // Check if the position is occupied by any of the black pieces
            return (blackPieces & positionMask) != 0;
        } else {
            // Check if the position is occupied by any of the white pieces
            return (whitePieces & positionMask) != 0;
        }
    }

    public boolean isEnPassantPossible(Position to, Color color) {
        // En passant is only possible if the last move made by the opponent was a pawn moving two steps from the starting rank
        if (lastMoveDoubleStepPawnPosition == null) {
            return false; // No pawn made the double step, or it's the first move of the game
        }

        // Calculate the rank where en passant is possible (which is rank 3 for white, rank 6 for black)
        int enPassantRank = color == Color.WHITE ? 6 : 3;

        // Check if the to position is at the correct rank for en passant
        if (to.getY() != enPassantRank) {
            return false;
        }

        // Check if the to position is next to the last move double step pawn
        int fileDifference = Math.abs(to.getX() - lastMoveDoubleStepPawnPosition.getX());
        if (fileDifference == 0 &&
                to.getY() == (lastMoveDoubleStepPawnPosition.getY() == 5 ? lastMoveDoubleStepPawnPosition.getY() + 1 : lastMoveDoubleStepPawnPosition.getY() - 1)) {
            return true;
        }

        return false;
    }

    public boolean hasKingMoved(Color color) {
        return (color == Color.WHITE) ? whiteKingMoved : blackKingMoved;
    }

    public boolean hasRookMoved(Position rookPosition) {
        if (rookPosition.equals(new Position('a', 1))) {
            return whiteRookA1Moved;
        } else if (rookPosition.equals(new Position('h', 1))) {
            return whiteRookH1Moved;
        } else if (rookPosition.equals(new Position('a', 8))) {
            return blackRookA8Moved;
        } else if (rookPosition.equals(new Position('h', 8))) {
            return blackRookH8Moved;
        }
        return true; // If the position doesn't match any rook starting position, assume it has moved
    }

    public boolean isOccupiedByPawn(Position position, Color color) {
        // Convert the position to the bit index
        int index = bitIndex(position.getX(), position.getY());

        // Create a bitmask for the position
        long positionMask = 1L << index;

        // Check if the position is occupied by a pawn of the given color
        if (color == Color.WHITE) {
            return (whitePawns & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPawns & positionMask) != 0;
        }
    }

    public boolean isOccupiedByKnight(Position position, Color color) {
        // Convert the position to the bit index
        int index = bitIndex(position.getX(), position.getY());

        // Create a bitmask for the position
        long positionMask = 1L << index;

        // Check if the position is occupied by a knight of the given color
        if (color == Color.WHITE) {
            return (whiteKnights & positionMask) != 0;
        } else { // Color.BLACK
            return (blackKnights & positionMask) != 0;
        }
    }

    public boolean isOccupiedByRook(Position position, Color color) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;
        if (color == Color.WHITE) {
            return (whiteRooks & positionMask) != 0;
        } else {
            return (blackRooks & positionMask) != 0;
        }
    }

    public boolean isOccupiedByQueen(Position position, Color color) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;
        if (color == Color.WHITE) {
            return (whiteQueens & positionMask) != 0;
        } else {
            return (blackQueens & positionMask) != 0;
        }
    }

    public boolean isOccupiedByBishop(Position position, Color color) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;
        if (color == Color.WHITE) {
            return (whiteBishops & positionMask) != 0;
        } else {
            return (blackBishops & positionMask) != 0;
        }
    }

    public boolean isOccupiedByKing(Position position, Color color) {
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;
        if (color == Color.WHITE) {
            return (whiteKing & positionMask) != 0;
        } else {
            return (blackKing & positionMask) != 0;
        }
    }

    public Position getLastMoveDoubleStepPawnPosition() {
        return this.lastMoveDoubleStepPawnPosition;
    }

    public boolean isOccupiedByColor(Position position, Color color) {
        // Convert the position to a bit index
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;

        // Check if the position is occupied by a piece of the given color
        if (color == Color.WHITE) {
            return (whitePieces & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPieces & positionMask) != 0;
        }
    }

    public List<Move> generateMovesForPieceAtPosition(PieceType pieceType, Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // Depending on the piece type, call the appropriate method to generate its moves
        switch (pieceType) {
            case PAWN -> moves.addAll(generatePawnMovesFromPosition(color, fromPosition));
            case KNIGHT -> moves.addAll(generateKnightMovesFromPosition(color, fromPosition));
            case BISHOP -> moves.addAll(generateBishopMovesFromPosition(color, fromPosition));
            case ROOK -> moves.addAll(generateRookMovesFromPosition(color, fromPosition));
            case QUEEN -> moves.addAll(generateQueenMovesFromPosition(color, fromPosition));
            case KING -> moves.addAll(generateKingMovesFromPosition(color, fromPosition));
            default -> throw new IllegalArgumentException("Unknown piece type: " + pieceType);
        }

        return moves;
    }

    // Each of these methods would need to be implemented to handle the specific move generation for each piece type.
    private List<Move> generatePawnMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // Determine the direction pawns will move based on their color
        int direction = (color == Color.WHITE) ? 1 : -1;

        // Position in front of the pawn
        Position oneStep = new Position(fromPosition.getX(), fromPosition.getY() + direction);
        // If the position in front of the pawn is not occupied, it's a valid move
        if (!isOccupied(oneStep)) {
            moves.add(new Move(fromPosition, oneStep, PieceType.PAWN, color, false, false, false, null));

            // If it's the pawn's first move, it can move two squares
            if ((color == Color.WHITE && fromPosition.getY() == 2) || (color == Color.BLACK && fromPosition.getY() == 7)) {
                Position twoSteps = new Position(fromPosition.getX(), fromPosition.getY() + (2 * direction));
                if (!isOccupied(twoSteps)) {
                    moves.add(new Move(fromPosition, twoSteps, PieceType.PAWN, color, false, false, false, null));
                }
            }
        }

        // Capture moves
        Position[] capturePositions = {
                new Position((char) (fromPosition.getX() + 1), fromPosition.getY() + direction),
                new Position((char) (fromPosition.getX() - 1), fromPosition.getY() + direction)
        };

        for (Position capturePos : capturePositions) {
            if (isValidBoardPosition(capturePos) && isOccupiedByOpponent(capturePos, color)) {
                moves.add(new Move(fromPosition, capturePos, PieceType.PAWN, color, true, false, false, null));
            }
        }

        // En passant capture
        // En passant capture
        Position enPassantPosition = getLastMoveDoubleStepPawnPosition();
        if (enPassantPosition != null) {
            // The en passant capture can only happen if the pawn is on its 5th rank (for white) or 4th rank (for black)
            int enPassantRank = (color == Color.WHITE) ? 5 : 4;
            if (fromPosition.getY() == enPassantRank) {
                int enPassantFileOffset = enPassantPosition.getX() - fromPosition.getX();
                // Check if the en passant position is directly to the left or right of the current pawn
                if (Math.abs(enPassantFileOffset) == 1) {
                    // Calculate the capture position, which is one rank forward from the current pawn's position
                    // in the direction of the en passant position's file
                    Position capturePosition = new Position(enPassantPosition.getX(), fromPosition.getY() + direction);
                    // Add en passant capture move
                    moves.add(new Move(fromPosition, capturePosition, PieceType.PAWN, color, true, false, false, null));
                }
            }
        }

        // Promotion
        // If the pawn reaches the back rank, it should be promoted. You'll need to create moves for each promotion option.
        // This is a simplified version and doesn't create all the promotion moves
        if ((color == Color.WHITE && fromPosition.getY() == 7) || (color == Color.BLACK && fromPosition.getY() == 2)) {
            Position promotionPosition = new Position(fromPosition.getX(), fromPosition.getY() + direction);
            if (!isOccupied(promotionPosition)) {
                moves.add(new Move(fromPosition, promotionPosition, PieceType.PAWN, color, false, false, true, PieceType.QUEEN)); // Promotion to queen as an example
            }
        }

        return moves;
    }

    private boolean isValidBoardPosition(Position position) {
        return position.getX() >= 'a' && position.getX() <= 'h' && position.getY() >= 1 && position.getY() <= 8;
    }

    private List<Move> generateKnightMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // The possible moves for a knight from its current position
        int[][] knightOffsets = {
                {-2, -1}, {-2, 1}, // Upwards L-moves
                {2, -1}, {2, 1},   // Downwards L-moves
                {-1, -2}, {1, -2}, // Leftwards L-moves
                {-1, 2}, {1, 2}    // Rightwards L-moves
        };

        // Iterate through all possible L moves
        for (int[] offset : knightOffsets) {
            int targetFile = fromPosition.getX() + offset[0];
            int targetRank = fromPosition.getY() + offset[1];

            // Check if the target position is within the board limits
            if (targetFile >= 'a' && targetFile <= 'h' && targetRank >= 1 && targetRank <= 8) {
                Position toPosition = new Position((char) targetFile, targetRank);

                // Check if the target position is occupied by a piece of the same color
                if (!isOccupiedByColor(toPosition, color)) {
                    // Determine if the target position is occupied by an opponent's piece, which would make it a capture
                    boolean isCapture = isOccupiedByColor(toPosition, getOpponentColor(color));

                    // If it's a valid move, add it to the list, either to an empty square or capturing an opponent's piece
                    moves.add(new Move(fromPosition, toPosition, PieceType.KNIGHT, color, isCapture, false, false, null));
                }
            }
        }

        return moves;
    }

    private List<Move> generateBishopMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // The possible directions for a bishop's move (diagonals)
        int[][] directions = {
                {1, 1},   // Up-Right
                {1, -1},  // Down-Right
                {-1, -1}, // Down-Left
                {-1, 1}   // Up-Left
        };

        // Iterate through each direction a bishop can move
        for (int[] direction : directions) {
            int targetFile = fromPosition.getX();
            int targetRank = fromPosition.getY();

            // Keep moving in the direction until you hit the edge of the board or another piece
            while (true) {
                targetFile += direction[0];
                targetRank += direction[1];

                // Check if the new position is within the board
                if (targetFile >= 'a' && targetFile <= 'h' && targetRank >= 1 && targetRank <= 8) {
                    Position toPosition = new Position((char) targetFile, targetRank);

                    // If the position is occupied by a piece of the same color, break the loop, can't jump over
                    if (isOccupiedByColor(toPosition, color)) {
                        break;
                    }

                    // If it's an opponent's piece, it's a capture move
                    boolean isCapture = isOccupiedByColor(toPosition, getOpponentColor(color));

                    moves.add(new Move(fromPosition, toPosition, PieceType.BISHOP, color, isCapture, false, false, null));

                    // If you capture a piece, you must stop.
                    if (isCapture) {
                        break;
                    }
                } else {
                    // If the position is off the board, stop checking this direction
                    break;
                }
            }
        }

        return moves;
    }


    private List<Move> generateRookMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // The possible directions for a rook's move: horizontal and vertical
        int[][] directions = {
                {0, 1},  // Up
                {0, -1}, // Down
                {1, 0},  // Right
                {-1, 0}  // Left
        };

        // Iterate through each direction a rook can move
        for (int[] direction : directions) {
            int targetFile = fromPosition.getX();
            int targetRank = fromPosition.getY();

            // Keep moving in the direction until you hit the edge of the board or another piece
            while (true) {
                targetFile += direction[0];
                targetRank += direction[1];

                // Check if the new position is on the board
                if (targetFile >= 'a' && targetFile <= 'h' && targetRank >= 1 && targetRank <= 8) {
                    Position toPosition = new Position((char) targetFile, targetRank);

                    // If the position is occupied by a piece of the same color, break the loop, can't jump over
                    if (isOccupiedByColor(toPosition, color)) {
                        break;
                    }

                    // If it's an opponent's piece, it's a capture move
                    boolean isCapture = isOccupiedByColor(toPosition, getOpponentColor(color));

                    moves.add(new Move(fromPosition, toPosition, PieceType.ROOK, color, isCapture, false, false, null));

                    // If you capture a piece, you must stop.
                    if (isCapture) {
                        break;
                    }
                } else {
                    // If the position is off the board, stop checking this direction
                    break;
                }
            }
        }

        return moves;
    }


    private List<Move> generateQueenMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // Combine the moves of the rook and bishop for the queen
        moves.addAll(generateRookMovesFromPosition(color, fromPosition));
        moves.addAll(generateBishopMovesFromPosition(color, fromPosition));

        return moves;
    }


    private List<Move> generateKingMovesFromPosition(Color color, Position fromPosition) {
        List<Move> moves = new ArrayList<>();

        // The possible moves for a king from its current position
        int[][] kingOffsets = {
                {-1, -1}, {-1, 0}, {-1, 1}, // Diagonal and straight upwards
                {0, -1}, {0, 1},            // Straight sideways
                {1, -1}, {1, 0}, {1, 1}     // Diagonal and straight downwards
        };

        for (int[] offset : kingOffsets) {

            int targetFile = fromPosition.getX() + offset[0];
            int targetRank = fromPosition.getY() + offset[1];

            // Check if the target position is within the board limits
            if (targetFile >= 'a' && targetFile <= 'h' && targetRank >= 1 && targetRank <= 8) {
                Position toPosition = new Position((char) targetFile, targetRank);

                // Check if the target position is occupied by a piece of the same color or if the king would be in check after moving
                if (!isOccupiedByColor(toPosition, color) && !isSquareUnderAttack(toPosition, color)) {
                    boolean isCapture = isOccupiedByOpponent(toPosition, color);
                    moves.add(new Move(fromPosition, toPosition, PieceType.KING, color, isCapture, false, false, null));
                }
            }
        }

        // Check for castling if the king has not moved and is not in check
        if (!hasKingMoved(color) && !isInCheck(color)) {
            // Kingside castling
            if (canCastleKingside(color, fromPosition)) {
                moves.add(new Move(fromPosition, new Position((char) (fromPosition.getX() + 2), fromPosition.getY()), PieceType.KING, color, false, true, false, null));
            }
            // Queenside castling
            if (canCastleQueenside(color, fromPosition)) {
                moves.add(new Move(fromPosition, new Position((char) (fromPosition.getX() - 2), fromPosition.getY()), PieceType.KING, color, false, true, false, null));
            }
        }

        return moves;
    }

    private boolean canCastleQueenside(Color color, Position kingPosition) {

        // Ensure the squares between the king and the rook are unoccupied and not under attack
        Position[] queensideSquares = {
                new Position((char) (kingPosition.getX() - 1), kingPosition.getY()),
                new Position((char) (kingPosition.getX() - 2), kingPosition.getY()),
                new Position((char) (kingPosition.getX() - 3), kingPosition.getY())
        };

        for (Position square : queensideSquares) {
            if (isOccupied(square) || isSquareUnderAttack(square, color)) {
                return false;
            }
        }
        return !hasRookMoved(new Position('a', color == Color.WHITE ? 1 : 8));
    }

    private boolean canCastleKingside(Color color, Position kingPosition) {

        // Ensure the squares between the king and the rook are unoccupied and not under attack
        Position[] kingsideSquares = {
                new Position((char) (kingPosition.getX() + 1), kingPosition.getY()),
                new Position((char) (kingPosition.getX() + 2), kingPosition.getY())
        };

        for (Position square : kingsideSquares) {
            if (isOccupied(square) || isSquareUnderAttack(square, color)) {
                return false;
            }
        }
        return !hasRookMoved(new Position('h', color == Color.WHITE ? 1 : 8));
    }

// The canCastleKingside and canCastleQueenside methods need to be implemented to check if castling is possible
// These methods would check for the rooks having moved, the squares between the king and rook being unoccupied and not under attack, etc.


    private Color getOpponentColor(Color color) {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public boolean isInCheck(Color color) {
        // Find the position of the king for the given color
        Position kingPosition = findKingPosition(color);

        // Check attacks from pawns
        if (canPawnAttackKing(kingPosition, color)) {
            return true;
        }

        // Check attacks from knights
        if (canKnightAttackKing(kingPosition, color)) {
            return true;
        }

        // Check attacks from bishops
        if (canBishopAttackKing(kingPosition, color)) {
            return true;
        }

        // Check attacks from rooks
        if (canRookAttackKing(kingPosition, color)) {
            return true;
        }

        // Check attacks from queens
        if (canQueenAttackKing(kingPosition, color)) {
            return true;
        }

        // Check if the opposing king can attack
        if (canKingAttackKing(kingPosition, color)) {
            return true;
        }

        // If none of the pieces can attack the king, then the king is not in check
        return false;
    }

    private Position findKingPosition(Color color) {
        // Use bit operations to find the king's position on the board
        // Assuming there's only one king per color on the board.
        long kingBitboard = (color == Color.WHITE) ? whiteKing : blackKing;
        int index = Long.numberOfTrailingZeros(kingBitboard);
        return indexToPosition(index);
    }

    // Example for one attack check - similar methods needed for other piece types
    private boolean canPawnAttackKing(Position kingPosition, Color kingColor) {
        // Determine the direction from which the pawn would attack
        int pawnAttackDirection = (kingColor == Color.WHITE) ? -1 : 1;
        // Generate the positions from which an opponent pawn would attack
        Position attackFromLeft = new Position((char) (kingPosition.getX() - 1), kingPosition.getY() + pawnAttackDirection);
        Position attackFromRight = new Position((char) (kingPosition.getX() + 1), kingPosition.getY() + pawnAttackDirection);
        // Check if there is an opponent pawn in either of those positions
        Color opponentColor = (kingColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        return (isOccupiedByPawn(attackFromLeft, opponentColor) || isOccupiedByPawn(attackFromRight, opponentColor));
    }

    private boolean canKnightAttackKing(Position kingPosition, Color kingColor) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the knight attack bitmask from the king's position
        long knightAttacks = knightAttackBitmask(kingIndex);

        // Determine the opponent's knights bitboard based on the king's color
        long opponentKnights = (kingColor == Color.WHITE) ? blackKnights : whiteKnights;

        // Check if the opponent has knights that can attack the king's position
        return (knightAttacks & opponentKnights) != 0;
    }

    private long knightAttackBitmask(int positionIndex) {
        // Define knight move offsets
        int[] knightMoves = {
                -17, -15, -10, -6, 6, 10, 15, 17
        };

        long attacks = 0L;
        for (int moveOffset : knightMoves) {
            int targetIndex = positionIndex + moveOffset;

            // Check if the target index is on the board
            if (targetIndex >= 0 && targetIndex < 64) {
                // Calculate the file and rank of the target index
                int file = targetIndex % 8;
                int rank = targetIndex / 8;

                // Calculate the file and rank of the original position index
                int originalFile = positionIndex % 8;
                int originalRank = positionIndex / 8;

                // Check if the move stays within the bounds of the chess board
                if (Math.abs(file - originalFile) <= 2 && Math.abs(rank - originalRank) <= 2) {
                    // Set the bit at the target index
                    attacks |= 1L << targetIndex;
                }
            }
        }

        return attacks;
    }

    private boolean canBishopAttackKing(Position kingPosition, Color kingColor) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the bishop attack bitmask from the king's position
        long bishopAttacks = bishopAttackBitmask(kingIndex);

        // Determine the opponent's bishops bitboard based on the king's color
        long opponentBishops = (kingColor == Color.WHITE) ? blackBishops : whiteBishops;

        // Check if the opponent has bishops that can attack the king's position
        return (bishopAttacks & opponentBishops) != 0;
    }

    private long bishopAttackBitmask(int positionIndex) {
        long attacks = 0L;

        // Directions a bishop can move: top-left, top-right, bottom-left, bottom-right
        int[][] directions = {{-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

        // Generate moves for each direction
        for (int[] direction : directions) {
            int targetIndex = positionIndex;
            while (true) {
                // Apply the direction to the targetIndex
                int targetFile = (targetIndex % 8) + direction[0];
                int targetRank = (targetIndex / 8) + direction[1];

                // If the target is outside the board, break the loop
                if (targetFile < 0 || targetFile >= 8 || targetRank < 0 || targetRank >= 8) {
                    break;
                }

                // Calculate the new target index and add it to the bitmask
                targetIndex = targetRank * 8 + targetFile;
                attacks |= 1L << targetIndex;

                // If there is a piece at the target index, break the loop
                if ((allPieces & (1L << targetIndex)) != 0) {
                    break;
                }
            }
        }

        return attacks;
    }

    private boolean canRookAttackKing(Position kingPosition, Color kingColor) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the rook attack bitmask from the king's position
        long rookAttacks = rookAttackBitmask(kingIndex);

        // Determine the opponent's rooks bitboard based on the king's color
        long opponentRooks = (kingColor == Color.WHITE) ? blackRooks : whiteRooks;

        // Check if the opponent has rooks that can attack the king's position
        return (rookAttacks & opponentRooks) != 0;
    }

    private long rookAttackBitmask(int positionIndex) {
        long attacks = 0L;

        // Directions a rook can move: up, down, left, right
        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

        // Generate moves for each direction
        for (int[] direction : directions) {
            int targetIndex = positionIndex;
            while (true) {
                // Apply the direction to the targetIndex
                int targetFile = (targetIndex % 8) + direction[0];
                int targetRank = (targetIndex / 8) + direction[1];

                // If the target is outside the board, break the loop
                if (targetFile < 0 || targetFile >= 8 || targetRank < 0 || targetRank >= 8) {
                    break;
                }

                // Calculate the new target index and add it to the bitmask
                targetIndex = targetRank * 8 + targetFile;
                attacks |= 1L << targetIndex;

                // If there is a piece at the target index, break the loop
                if ((allPieces & (1L << targetIndex)) != 0) {
                    break;
                }
            }
        }

        return attacks;
    }

    private boolean canQueenAttackKing(Position kingPosition, Color kingColor) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the queen attack bitmask from the king's position
        long queenAttacks = queenAttackBitmask(kingIndex);

        // Determine the opponent's queens bitboard based on the king's color
        long opponentQueens = (kingColor == Color.WHITE) ? blackQueens : whiteQueens;

        // Check if the opponent has queens that can attack the king's position
        return (queenAttacks & opponentQueens) != 0;
    }

    private long queenAttackBitmask(int positionIndex) {
        // The queen's attack bitmask is a combination of the rook's and bishop's attack bitmasks
        long rookAttacks = rookAttackBitmask(positionIndex);
        long bishopAttacks = bishopAttackBitmask(positionIndex);

        // Combine both attack patterns
        return rookAttacks | bishopAttacks;
    } // The rookAttackBitmask and bishopAttackBitmask methods would be similar to the ones implemented in previous answers.

    private boolean canKingAttackKing(Position kingPosition, Color kingColor) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the king attack bitmask from the king's position
        long kingAttacks = kingAttackBitmask(kingIndex);

        // Determine the opponent's king bitboard based on the king's color
        long opponentKing = (kingColor == Color.WHITE) ? blackKing : whiteKing;

        // Check if the opponent's king is on one of the squares that the current king can attack
        return (kingAttacks & opponentKing) != 0;
    }

    private long kingAttackBitmask(int positionIndex) {
        // Define king move offsets (all the adjacent squares)
        int[] kingMoves = {
                -9, -8, -7, -1, 1, 7, 8, 9
        };

        long attacks = 0L;
        for (int moveOffset : kingMoves) {
            int targetIndex = positionIndex + moveOffset;

            // Check if the target index is on the board
            if (targetIndex >= 0 && targetIndex < 64) {
                // Calculate the file and rank of the target index
                int file = targetIndex % 8;
                int rank = targetIndex / 8;

                // Calculate the file and rank of the original position index
                int originalFile = positionIndex % 8;
                int originalRank = positionIndex / 8;

                // Check if the move stays within the bounds of the chess board (kings cannot move off their own file or rank more than one square)
                if (Math.abs(file - originalFile) <= 1 && Math.abs(rank - originalRank) <= 1) {
                    // Set the bit at the target index
                    attacks |= 1L << targetIndex;
                }
            }
        }

        return attacks;
    }


    // Convert bit index back to board position
    public Position indexToPosition(int index) {
        char file = (char) ('a' + index % 8);
        int rank = 1 + index / 8;
        return new Position(file, rank);
    }

    public List<Figure> getFigures() {
        List<Figure> figures = new ArrayList<>();

        // Iterate over all squares of the board
        for (int index = 0; index < 64; index++) {
            Position position = indexToPosition(index);
            long positionMask = 1L << index;

            // Check for each piece type and color
            if ((whitePawns & positionMask) != 0) {
                figures.add(new Figure(PieceType.PAWN, Color.WHITE, position));
            } else if ((blackPawns & positionMask) != 0) {
                figures.add(new Figure(PieceType.PAWN, Color.BLACK, position));
            }
            if ((whiteKnights & positionMask) != 0) {
                figures.add(new Figure(PieceType.KNIGHT, Color.WHITE, position));
            } else if ((blackKnights & positionMask) != 0) {
                figures.add(new Figure(PieceType.KNIGHT, Color.BLACK, position));
            }
            if ((whiteBishops & positionMask) != 0) {
                figures.add(new Figure(PieceType.BISHOP, Color.WHITE, position));
            } else if ((blackBishops & positionMask) != 0) {
                figures.add(new Figure(PieceType.BISHOP, Color.BLACK, position));
            }
            if ((whiteRooks & positionMask) != 0) {
                figures.add(new Figure(PieceType.ROOK, Color.WHITE, position));
            } else if ((blackRooks & positionMask) != 0) {
                figures.add(new Figure(PieceType.ROOK, Color.BLACK, position));
            }
            if ((whiteQueens & positionMask) != 0) {
                figures.add(new Figure(PieceType.QUEEN, Color.WHITE, position));
            } else if ((blackQueens & positionMask) != 0) {
                figures.add(new Figure(PieceType.QUEEN, Color.BLACK, position));
            }
            if ((whiteKing & positionMask) != 0) {
                figures.add(new Figure(PieceType.KING, Color.WHITE, position));
            } else if ((blackKing & positionMask) != 0) {
                figures.add(new Figure(PieceType.KING, Color.BLACK, position));
            }
        }

        return figures;
    }

    public Score getScore() {
        // Define the piece values
        final int PAWN_VALUE = 100;   // Pawns are worth 1 point, scaled by 100
        final int KNIGHT_VALUE = 300; // Knights are worth 3 points
        final int BISHOP_VALUE = 300; // Bishops are worth 3 points
        final int ROOK_VALUE = 500;   // Rooks are worth 5 points
        final int QUEEN_VALUE = 900;  // Queens are worth 9 points

        // Initialize scores
        int whiteScore = 0;
        int blackScore = 0;

        // Calculate scores based on bitboards
        whiteScore += Long.bitCount(whitePawns) * PAWN_VALUE;
        whiteScore += Long.bitCount(whiteKnights) * KNIGHT_VALUE;
        whiteScore += Long.bitCount(whiteBishops) * BISHOP_VALUE;
        whiteScore += Long.bitCount(whiteRooks) * ROOK_VALUE;
        whiteScore += Long.bitCount(whiteQueens) * QUEEN_VALUE;

        blackScore += Long.bitCount(blackPawns) * PAWN_VALUE;
        blackScore += Long.bitCount(blackKnights) * KNIGHT_VALUE;
        blackScore += Long.bitCount(blackBishops) * BISHOP_VALUE;
        blackScore += Long.bitCount(blackRooks) * ROOK_VALUE;
        blackScore += Long.bitCount(blackQueens) * QUEEN_VALUE;

        // Factor in king safety, piece positions, control of center, etc., for a more advanced scoring
        // These advanced concepts are omitted for brevity, but would involve additional logic.

        // Return the score encapsulated in a Score object
        return new Score(whiteScore, blackScore);
    }

    public void logBoard() {
        StringBuilder logBoard = new StringBuilder();
        logBoard.append('\n');
        for (int rank = 8; rank >= 1; rank--) {
            for (char file = 'a'; file <= 'h'; file++) {
                int index = bitIndex(file, rank);
                long positionMask = 1L << index;

                // Determine the piece at the current position
                char pieceChar = '.';
                if ((whitePawns & positionMask) != 0) pieceChar = 'P';
                else if ((blackPawns & positionMask) != 0) pieceChar = 'p';
                else if ((whiteKnights & positionMask) != 0) pieceChar = 'N';
                else if ((blackKnights & positionMask) != 0) pieceChar = 'n';
                else if ((whiteBishops & positionMask) != 0) pieceChar = 'B';
                else if ((blackBishops & positionMask) != 0) pieceChar = 'b';
                else if ((whiteRooks & positionMask) != 0) pieceChar = 'R';
                else if ((blackRooks & positionMask) != 0) pieceChar = 'r';
                else if ((whiteQueens & positionMask) != 0) pieceChar = 'Q';
                else if ((blackQueens & positionMask) != 0) pieceChar = 'q';
                else if ((whiteKing & positionMask) != 0) pieceChar = 'K';
                else if ((blackKing & positionMask) != 0) pieceChar = 'k';

                // Add the piece character to the log board
                logBoard.append(pieceChar).append(' ');
            }
            logBoard.append("  ").append(rank).append('\n'); // Append the rank number at the end of each line
        }
        logBoard.append("  a b c d e f g h"); // Append file letters at the bottom
        log.info(logBoard.toString()); // Log the current board state
    }


    // ... Additional methods to work with bitboards
}
