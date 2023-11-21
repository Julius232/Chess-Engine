package julius.game.chessengine.board;

import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.helper.BishopHelper;
import julius.game.chessengine.helper.RookHelper;
import julius.game.chessengine.helper.ZobristTable;
import julius.game.chessengine.utils.Color;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

import static julius.game.chessengine.helper.BitHelper.*;
import static julius.game.chessengine.helper.KnightHelper.knightMoveTable;

@Log4j2
@Getter
public class BitBoard {

    BishopHelper bishopHelper = BishopHelper.getInstance();
    RookHelper rookHelper = RookHelper.getInstance();

    public boolean whitesTurn = true;
    // Add score field to the BitBoard class
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
    //TODO only write to it if en passant is possible then you can also hash it
    @Getter
    private int lastMoveDoubleStepPawnIndex;

    // Flags to track if the king and rooks have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookA1Moved = false;
    private boolean whiteRookH1Moved = false;
    private boolean blackRookA8Moved = false;
    private boolean blackRookH8Moved = false;

    //Warning those booleans do not fully work for FEN imported games, because it's impossible to determine if a king castled
    //I just need those values to give the Engine a better score if it castles
    private boolean whiteKingHasCastled = false;
    private boolean blackKingHasCastled = false;

    public BitBoard(boolean whitesTurn, long whitePawns, long blackPawns, long whiteKnights, long blackKnights, long whiteBishops, long blackBishops, long whiteRooks, long blackRooks, long whiteQueens, long blackQueens, long whiteKing, long blackKing, long whitePieces, long blackPieces, long allPieces, int lastMoveDoubleStepPawnIndex, boolean whiteKingMoved, boolean blackKingMoved, boolean whiteRookA1Moved, boolean whiteRookH1Moved, boolean blackRookA8Moved, boolean blackRookH8Moved, boolean whiteKingHasCastled, boolean blackKingHasCastled) {
        this.whitesTurn = whitesTurn;
        this.whitePawns = whitePawns;
        this.blackPawns = blackPawns;
        this.whiteKnights = whiteKnights;
        this.blackKnights = blackKnights;
        this.whiteBishops = whiteBishops;
        this.blackBishops = blackBishops;
        this.whiteRooks = whiteRooks;
        this.blackRooks = blackRooks;
        this.whiteQueens = whiteQueens;
        this.blackQueens = blackQueens;
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.allPieces = allPieces;
        this.lastMoveDoubleStepPawnIndex = lastMoveDoubleStepPawnIndex;
        this.whiteKingMoved = whiteKingMoved;
        this.blackKingMoved = blackKingMoved;
        this.whiteRookA1Moved = whiteRookA1Moved;
        this.whiteRookH1Moved = whiteRookH1Moved;
        this.blackRookA8Moved = blackRookA8Moved;
        this.blackRookH8Moved = blackRookH8Moved;
        this.whiteKingHasCastled = whiteKingHasCastled;
        this.blackKingHasCastled = blackKingHasCastled;
    }

    public BitBoard() {
        setInitialPosition();
    }

    public BitBoard(BitBoard other) {
        // Copying all the long fields representing the pieces
        this.bishopHelper = other.bishopHelper;
        this.rookHelper = other.rookHelper;

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

        this.whitesTurn = other.whitesTurn;

        this.lastMoveDoubleStepPawnIndex = other.lastMoveDoubleStepPawnIndex;

        this.blackKingHasCastled = other.blackKingHasCastled;
        this.whiteKingHasCastled = other.whiteKingHasCastled;
    }


    public boolean hasInsufficientMaterial() {
        // Early return if any side has pawns, rooks, or queens, as these can achieve checkmate
        if ((whitePawns | blackPawns | whiteRooks | blackRooks | whiteQueens | blackQueens) != 0) {
            return false;
        }

        // Count knights and bishops for both sides
        int whiteMinorPieces = Long.bitCount(whiteKnights) + Long.bitCount(whiteBishops);
        int blackMinorPieces = Long.bitCount(blackKnights) + Long.bitCount(blackBishops);

        // Check if both sides have insufficient material
        return (whiteMinorPieces <= 1) && (blackMinorPieces <= 1);
    }

    public MoveList getAllCurrentPossibleMoves() {
        return generateAllPossibleMoves(whitesTurn);
    }

    // Method to set up the initial position
    private void setInitialPosition() {
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

        lastMoveDoubleStepPawnIndex = -1;
    }

    // Method to get the bitboard for a specific piece type and color
    public long intToPiecesBitboard(int pieceTypeBits, boolean isWhite) {
        if (isWhite) {
            return switch (pieceTypeBits) {
                case 1 -> whitePawns;
                case 2 -> whiteKnights;
                case 3 -> whiteBishops;
                case 4 -> whiteRooks;
                case 5 -> whiteQueens;
                case 6 -> whiteKing;
                default -> 0;
            };
        } else {
            return switch (pieceTypeBits) {
                case 1 -> blackPawns;
                case 2 -> blackKnights;
                case 3 -> blackBishops;
                case 4 -> blackRooks;
                case 5 -> blackQueens;
                case 6 -> blackKing;
                default -> 0;
            };
        }
    }

    private void setBitboardForPiece(int pieceTypeBits, boolean isWhite, long bitboard) {
        if (isWhite) {
            switch (pieceTypeBits) {
                case 1 -> whitePawns = bitboard;
                case 2 -> whiteKnights = bitboard;
                case 3 -> whiteBishops = bitboard;
                case 4 -> whiteRooks = bitboard;
                case 5 -> whiteQueens = bitboard;
                case 6 -> whiteKing = bitboard;
                default -> throw new IllegalArgumentException("Unknown piece type: " + pieceTypeBits);
            }

        } else {
            switch (pieceTypeBits) {
                case 1 -> blackPawns = bitboard;
                case 2 -> blackKnights = bitboard;
                case 3 -> blackBishops = bitboard;
                case 4 -> blackRooks = bitboard;
                case 5 -> blackQueens = bitboard;
                case 6 -> blackKing = bitboard;
                default -> throw new IllegalArgumentException("Unknown piece type: " + pieceTypeBits);
            }
        }

        // After setting the bitboard, update the aggregated bitboards
        updateAggregatedBitboards();
    }
    // Call these methods within the movePiece method when a king or rook moves

    private void markKingAsMoved(boolean isWhite) {
        if (isWhite) {
            whiteKingMoved = true;
        } else {
            blackKingMoved = true;
        }
    }

    public MoveList generateAllPossibleMoves(boolean whitesTurn) {
        MoveList moves = new MoveList();

        //on average there are about 30 possible moves in a chess position
        // Generate moves for each piece type
        generatePawnMoves(whitesTurn, moves);
        generateKnightMoves(whitesTurn, moves);
        generateBishopMoves(whitesTurn, moves);
        generateRookMoves(whitesTurn, moves);
        generateQueenMoves(whitesTurn, moves);
        generateKingMoves(whitesTurn, moves);

        return moves;
    }

    // Method to set the bitboard for a specific piece type and color
    void updateAggregatedBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }

    private void generatePawnMoves(boolean whitesTurn, MoveList moves) {
        long pawns = whitesTurn ? whitePawns : blackPawns;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;
        long emptySquares = ~(whitePieces | blackPieces);

        long singleStepForward = whitesTurn ? pawns << 8 : pawns >>> 8;
        singleStepForward &= emptySquares;

        long doubleStepForward;
        if (whitesTurn) {
            doubleStepForward = ((pawns & RankMasks[1]) << 8 & emptySquares) << 8 & emptySquares;
        } else {
            doubleStepForward = ((pawns & RankMasks[6]) >>> 8 & emptySquares) >>> 8 & emptySquares;
        }

        long attacksLeft = whitesTurn ? (pawns & ~FileMasks[0]) << 7 : (pawns & ~FileMasks[0]) >>> 9;
        long attacksRight = whitesTurn ? (pawns & ~FileMasks[7]) << 9 : (pawns & ~FileMasks[7]) >>> 7;
        attacksLeft &= opponentPieces;
        attacksRight &= opponentPieces;

        addPawnMoves(moves, singleStepForward, 8, false, whitesTurn);
        addPawnMoves(moves, doubleStepForward, 16, false, whitesTurn);
        addPawnMoves(moves, attacksLeft, whitesTurn ? 7 : 9, true, whitesTurn);
        addPawnMoves(moves, attacksRight, whitesTurn ? 9 : 7, true, whitesTurn);

        if (lastMoveDoubleStepPawnIndex != -1) {
            generateEnPassantMoves(moves, pawns, whitesTurn);
        }

    }

    private void generateEnPassantMoves(MoveList moves, long pawns, boolean whitesTurn) {
        int enPassantRank = whitesTurn ? 5 : 2;
        int fileIndexOfDoubleSteppedPawn = lastMoveDoubleStepPawnIndex % 8;
        int enPassantTargetIndex = (enPassantRank * 8) + fileIndexOfDoubleSteppedPawn;
        long enPassantTargetSquare = 1L << enPassantTargetIndex;
        long potentialEnPassantAttackers = pawns & RankMasks[whitesTurn ? 4 : 3];

        if (fileIndexOfDoubleSteppedPawn > 0) {
            long leftAttackers = potentialEnPassantAttackers & FileMasks[fileIndexOfDoubleSteppedPawn - 1];
            if (whitesTurn ?
                    ((leftAttackers << 9 & enPassantTargetSquare) != 0) :
                    ((leftAttackers >> 7 & enPassantTargetSquare) != 0)) {
                addEnPassantMove(moves, Long.numberOfTrailingZeros(leftAttackers), enPassantTargetIndex, whitesTurn);
            }
        }

        if (fileIndexOfDoubleSteppedPawn < 7) {
            long rightAttackers = potentialEnPassantAttackers & FileMasks[fileIndexOfDoubleSteppedPawn + 1];
            if (whitesTurn ?
                    ((rightAttackers << 7 & enPassantTargetSquare) != 0) :
                    ((rightAttackers >> 9 & enPassantTargetSquare) != 0)) {
                addEnPassantMove(moves, Long.numberOfTrailingZeros(rightAttackers), enPassantTargetIndex, whitesTurn);
            }
        }
    }


    private void addEnPassantMove(MoveList moves, int fromIndex, int toIndex, boolean whitesTurn) {
        moves.add(
                createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, true, false, true, null, PieceType.PAWN, false, false));
    }

    private void addPawnMoves(MoveList moves, long bitboard, int shift, boolean isCapture, boolean whitesTurn) {
        int direction = whitesTurn ? 1 : -1;
        while (bitboard != 0) {
            int toIndex = Long.numberOfTrailingZeros(bitboard);
            int fromIndex = whitesTurn ? toIndex - shift : toIndex + shift;

            // Calculate if the move is a promotion
            boolean isPromotion = (whitesTurn && toIndex / 8 == 7) || (!whitesTurn && toIndex / 8 == 0);

            PieceType capturedType = isCapture ? getPieceTypeAtIndex(toIndex) : null;

            // Check for initial double square move
            if (checkForInitialDoubleSquareMove(fromIndex, toIndex, direction)) {
                if (isPromotion) {
                    moves.add(createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.QUEEN, capturedType, false, false));
                    moves.add(createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.ROOK, capturedType, false, false));
                    moves.add(createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.BISHOP, capturedType, false, false));
                    moves.add(createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.KNIGHT, capturedType, false, false));
                } else {
                    moves.add(createMoveInt(fromIndex, toIndex, PieceType.PAWN, whitesTurn, isCapture, false, false, null, capturedType, false, false));
                }
            }

            bitboard &= bitboard - 1; // Clear the processed bit
        }
    }

    private boolean checkForInitialDoubleSquareMove(int fromIndex, int toIndex, int direction) {
        int fromRow = fromIndex / 8;
        int toRow = toIndex / 8;
        int yDiff = toRow - fromRow; // No need for Math.abs as pawns only move forward

        // Check for capture
        if (yDiff == direction && ((fromIndex + toIndex) % 2 != 0)) {
            return isOccupiedByOpponent(toIndex, whitesTurn);
        }

        // Check for single forward move
        if (yDiff == direction) {
            return !isOccupied(toIndex);
        }

        // Check for double forward move
        if (yDiff == 2 * direction) {
            int inBetweenIndex = fromIndex + 8 * direction;
            return !isOccupied(toIndex) && !isOccupied(inBetweenIndex);
        }

        return false;
    }


    private void generateKnightMoves(boolean whitesTurn, MoveList moves) {
        long knights = whitesTurn ? whiteKnights : blackKnights;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;

        while (knights != 0) {
            int knightIndex = Long.numberOfTrailingZeros(knights);
            long potentialMoves = knightMoveTable[knightIndex]; // Use precomputed moves

            while (potentialMoves != 0) {
                int targetIndex = Long.numberOfTrailingZeros(potentialMoves);

                // Use parentheses to ensure the correct order of evaluation
                boolean isCapture = (opponentPieces & (1L << targetIndex)) != 0;
                boolean isOwnPiece = (ownPieces & (1L << targetIndex)) != 0;

                // Proceed if the target position is not occupied by own piece
                if (!isOwnPiece) {
                    PieceType capturedPieceType = isCapture ? getPieceTypeAtIndex(targetIndex) : null;
                    moves.add(createMoveInt(knightIndex, targetIndex, PieceType.KNIGHT, whitesTurn, isCapture, false, false, null, capturedPieceType, false, false));
                }

                potentialMoves &= potentialMoves - 1; // Clear the lowest set bit
            }

            knights &= knights - 1; // Clear the lowest set bit
        }
    }


    private void generateBishopMoves(boolean isWhite, MoveList moves) {
        long bishops = isWhite ? whiteBishops : blackBishops;
        long ownPieces = isWhite ? whitePieces : blackPieces;
        long opponentPieces = isWhite ? blackPieces : whitePieces;

        while (bishops != 0) {
            int bishopSquare = Long.numberOfTrailingZeros(bishops);
            bishops &= bishops - 1; // Remove the least significant bit representing a bishop

            long occupancy = allPieces & bishopHelper.bishopMasks[bishopSquare];
            long attacks = bishopHelper.calculateMovesUsingBishopMagic(bishopSquare, occupancy) & ~ownPieces;

            while (attacks != 0) {
                int targetSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1; // Remove the least significant bit representing an attack


                boolean isCapture = (opponentPieces & (1L << targetSquare)) != 0;
                moves.add(createMoveInt(bishopSquare, targetSquare, PieceType.BISHOP, isWhite, isCapture, false, false, null, isCapture ? getPieceTypeAtIndex(targetSquare) : null, false, false));
            }
        }
    }


    private void generateRookMoves(boolean whitesTurn, MoveList moves) {
        long rooks = whitesTurn ? whiteRooks : blackRooks;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;

        while (rooks != 0) {
            int rookSquare = Long.numberOfTrailingZeros(rooks);
            rooks &= rooks - 1; // Remove the least significant bit representing a rook

            // Use RookHelper to calculate rook moves using magic bitboards
            long occupancy = allPieces & rookHelper.rookMasks[rookSquare];
            long attacks = rookHelper.calculateMovesUsingRookMagic(rookSquare, occupancy) & ~ownPieces;

            while (attacks != 0) {
                int targetSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1; // Remove the least significant bit representing an attack
                boolean isFirstRookMove = !hasRookMoved(rookSquare);
                boolean isCapture = (opponentPieces & (1L << targetSquare)) != 0;
                moves.add(createMoveInt(rookSquare, targetSquare, PieceType.ROOK, whitesTurn, isCapture, false, false, null, isCapture ? getPieceTypeAtIndex(targetSquare) : null, false, isFirstRookMove));
            }
        }
    }

    private void generateQueenMoves(boolean whitesTurn, MoveList moves) {
        long queens = whitesTurn ? whiteQueens : blackQueens;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;

        while (queens != 0) {
            int queenSquare = Long.numberOfTrailingZeros(queens);
            queens &= queens - 1;
            long occupancyBishop = allPieces & bishopHelper.bishopMasks[queenSquare];
            long occupancyRook = allPieces & rookHelper.rookMasks[queenSquare];

            long attacks = (
                    bishopHelper.calculateMovesUsingBishopMagic(queenSquare, occupancyBishop) |
                    rookHelper.calculateMovesUsingRookMagic(queenSquare, occupancyRook)
            ) & ~ownPieces;
            while (attacks != 0) {
                int targetSquare = Long.numberOfTrailingZeros(attacks);
                attacks &= attacks - 1; // Remove the least significant bit representing an attack
                boolean isCapture = (opponentPieces & (1L << targetSquare)) != 0;
                moves.add(createMoveInt(queenSquare, targetSquare, PieceType.QUEEN, whitesTurn, isCapture, false, false, null, isCapture ? getPieceTypeAtIndex(targetSquare) : null, false, false));
            }
        }
    }

    private void generateKingMoves(boolean whitesTurn, MoveList moves) {
        long kingBitboard = whitesTurn ? whiteKing : blackKing;
        int kingPositionIndex = Long.numberOfTrailingZeros(kingBitboard);

        // Offsets for a king's normal moves
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};

        boolean isFirstKingMove = hasKingNotMoved(whitesTurn);

        for (int offset : offsets) {
            int targetIndex = kingPositionIndex + offset;
            // Check if the move is within board bounds and does not wrap around
            if (targetIndex >= 0 && targetIndex < 64 && !doesMoveWrapAround(kingPositionIndex, targetIndex)) {
                if (!isOccupiedByColor(targetIndex, whitesTurn)) {
                    boolean isCapture = isOccupiedByOpponent(targetIndex, whitesTurn);
                    PieceType capturedPieceType = isCapture ? getPieceTypeAtIndex(targetIndex) : null;
                    moves.add(createMoveInt(kingPositionIndex, targetIndex, PieceType.KING, whitesTurn, isCapture, false, false, null, capturedPieceType, isFirstKingMove, false));
                }
            }
        }

        // Castling logic
        // The actual castling checks should be implemented in the canCastleKingside and canCastleQueenside methods
        if (canKingCastle(whitesTurn)) {
            // Kingside castling
            if (canCastleKingside(whitesTurn, kingPositionIndex)) {
                moves.add(createMoveInt(kingPositionIndex, kingPositionIndex + 2, PieceType.KING, whitesTurn, false, true, false, null, null, true, true));
            }
            // Queenside castling
            if (canCastleQueenside(whitesTurn, kingPositionIndex)) {
                moves.add(createMoveInt(kingPositionIndex, kingPositionIndex - 2, PieceType.KING, whitesTurn, false, true, false, null, null, true, true));
            }
        }
    }

    private boolean canKingCastle(boolean whitesTurn) {
        // The king must not have moved and must not be in check
        return hasKingNotMoved(whitesTurn) && !isInCheck(whitesTurn);
    }

    private boolean canCastleKingside(boolean colorWhite, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] kingsideSquares = {kingPositionIndex + 1, kingPositionIndex + 2};
        for (int square : kingsideSquares) {
            if (isOccupied(square) || isSquareUnderAttack(square, colorWhite)) {
                return false;
            }
        }

        int rookIndex = colorWhite ? 7 : 63;

        // Check if the rook has moved
        if (hasRookMoved(rookIndex)) {
            return false;
        }

        // Check if the rook still exists

        return isRookAtIndex(rookIndex);
    }

    private boolean canCastleQueenside(boolean colorWhite, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] queensideSquares = {kingPositionIndex - 1, kingPositionIndex - 2, kingPositionIndex - 3};
        for (int square : queensideSquares) {
            if (isOccupied(square) || (square != kingPositionIndex - 3 && isSquareUnderAttack(square, colorWhite))) {
                return false;
            }
        }

        int rookIndex = colorWhite ? 0 : 56;

        if (hasRookMoved(rookIndex)) {
            return false;
        }
        return isRookAtIndex(rookIndex);
    }

    private boolean isRookAtIndex(int index) {
        PieceType pieceAtPosition = getPieceTypeAtIndex(index);
        return pieceAtPosition == PieceType.ROOK;
    }

    private boolean isSquareUnderAttack(int index, boolean colorWhite) {
        boolean opponentColorWhite = !colorWhite;
        return canPawnAttackIndex(index, opponentColorWhite) ||
                canKnightAttackIndex(index, opponentColorWhite) ||
                canBishopAttackIndex(index, opponentColorWhite) ||
                canRookAttackIndex(index, opponentColorWhite) ||
                canQueenAttackIndex(index, opponentColorWhite) ||
                canKingAttackIndex(index, opponentColorWhite);
    }

    private boolean canPawnAttackIndex(int index, boolean colorWhite) {
        int direction = colorWhite ? -1 : 1; // Pawns move up if white, down if black
        int rank = index / 8;
        int file = index % 8;

        // Calculate indices of squares from which an opponent pawn could attack
        int attackFromLeftIndex = (rank + direction) * 8 + (file - 1);
        int attackFromRightIndex = (rank + direction) * 8 + (file + 1);

        return (isValidBoardIndex(attackFromLeftIndex) && isOccupiedByPawn(attackFromLeftIndex, colorWhite)) ||
                (isValidBoardIndex(attackFromRightIndex) && isOccupiedByPawn(attackFromRightIndex, colorWhite));
    }

    private boolean isValidBoardIndex(int index) {
        return index >= 0 && index < 64;
    }

    private boolean canKnightAttackIndex(int index, boolean colorWhite) {
        long knightsBitboard = colorWhite ? whiteKnights : blackKnights;
        long knightAttacks = knightAttackBitmask(index);
        return (knightAttacks & knightsBitboard) != 0;
    }

    private boolean canBishopAttackIndex(int index, boolean colorWhite) {
        long bishopsBitboard = colorWhite ? whiteBishops : blackBishops;
        long bishopAttacks = bishopAttackBitmask(index);
        return (bishopAttacks & bishopsBitboard) != 0;
    }

    private boolean canRookAttackIndex(int index, boolean colorWhite) {
        long rooksBitboard = colorWhite ? whiteRooks : blackRooks;
        long rookAttacks = rookAttackBitmask(index);
        return (rookAttacks & rooksBitboard) != 0;
    }

    private boolean canQueenAttackIndex(int index, boolean colorWhite) {
        long queensBitboard = colorWhite ? whiteQueens : blackQueens;
        long queenAttacks = queenAttackBitmask(index);
        return (queenAttacks & queensBitboard) != 0;
    }

    private boolean canKingAttackIndex(int index, boolean colorWhite) {
        long kingsBitboard = colorWhite ? whiteKing : blackKing;
        long kingAttacks = kingAttackBitmask(index);
        return (kingAttacks & kingsBitboard) != 0;
    }


    // The above-mentioned bishopAttackBitmask, rookAttackBitmask, queenAttackBitmask, and kingAttackBitmask methods
// would generate all potential squares each piece can attack from a given position and compare it to the current
// bitboard of that piece type to see if there's an overlap.
    public void performMove(int moveInt) {
        int fromIndex = moveInt & 0x3F; // Extract the first 6 bits
        int toIndex = (moveInt >> 6) & 0x3F; // Extract the next 6 bits
        int pieceTypeBits = (moveInt >> 12) & 0x07; // Extract the next 3 bits
        boolean isWhite = (moveInt & (1 << 15)) != 0; // Extract the color bit
        int specialProperty = (moveInt >> 16) & 0x03; // Extract the next 2 bits
        boolean isCapture = (specialProperty & 0x01) != 0;
        boolean isEnPassantMove = specialProperty == 3;
        boolean isCastlingMove = specialProperty == 2;
        int promotionPieceTypeBits = (moveInt >> 18) & 0x07; // Extract the next 3 bits

        long pieceBitboard = intToPiecesBitboard(pieceTypeBits, isWhite);

        if (isCapture) {
            clearSquare(toIndex, !isWhite);
        }

        // If the move is a castling move, move both the king and the rook
        if (isCastlingMove) {
            if (isWhite) {
                whiteKingHasCastled = true;
            } else {
                blackKingHasCastled = true;
            }
            // Determine if this is kingside or queenside castling
            boolean kingside = toIndex > fromIndex;
            int rookFromIndex, rookToIndex;
            if (kingside) {
                rookFromIndex = isWhite ? 7 : 63;
                rookToIndex = rookFromIndex - 2;
            } else {
                rookFromIndex = isWhite ? 0 : 56;
                rookToIndex = rookFromIndex + 3;
            }
            long rookBitboard = intToPiecesBitboard(4, isWhite);
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(4, isWhite, rookBitboard);

            // Mark the rook as moved
            markRookAsMoved(rookFromIndex);
        }

        // Move the piece
        pieceBitboard = moveBit(pieceBitboard, fromIndex, toIndex);
        setBitboardForPiece(pieceTypeBits, isWhite, pieceBitboard);

        if (promotionPieceTypeBits != 0) {
            // Clear the pawn from the promotion square
            clearSquare(toIndex, isWhite);

            // Set the bitboard for the promotion piece
            long promotionPieceBitboard = intToPiecesBitboard(promotionPieceTypeBits, isWhite);
            promotionPieceBitboard |= (1L << toIndex); // Place the promotion piece on the promotion square
            setBitboardForPiece(promotionPieceTypeBits, isWhite, promotionPieceBitboard);
        }

        // Mark the king as moved if it was a king move
        if (pieceTypeBits == 6) {
            markKingAsMoved(isWhite);
        }

        //Mark rook as moved
        if (pieceTypeBits == 4) {
            markRookAsMoved(fromIndex);
        }

        if (pieceTypeBits == 1 && Math.abs(fromIndex / 8 - toIndex / 8) == 2) {
            lastMoveDoubleStepPawnIndex = toIndex;
        } else {
            lastMoveDoubleStepPawnIndex = -1;
        }

        // Handle en passant capture
        if (isEnPassantMove) {
            // Clear the captured pawn for en passant
            int capturedPawnIndex = isWhite ? toIndex - 8 : toIndex + 8;
            clearSquare(capturedPawnIndex, !isWhite);
        }

        updateAggregatedBitboards();
        whitesTurn = !whitesTurn;
    }


    public void clearSquare(int index, boolean isWhite) {
        long mask = ~(1L << index);
        if (isWhite) {
            if ((whitePawns & (1L << index)) != 0L) whitePawns &= mask;
            if ((whiteKnights & (1L << index)) != 0L) whiteKnights &= mask;
            if ((whiteBishops & (1L << index)) != 0L) whiteBishops &= mask;
            if ((whiteRooks & (1L << index)) != 0L) whiteRooks &= mask;
            if ((whiteQueens & (1L << index)) != 0L) whiteQueens &= mask;  // Corrected line for queen
            whiteKing &= mask; // Only clear if the king is actually on the square
        } else {
            if ((blackPawns & (1L << index)) != 0L) blackPawns &= mask;
            if ((blackKnights & (1L << index)) != 0L) blackKnights &= mask;
            if ((blackBishops & (1L << index)) != 0L) blackBishops &= mask;
            if ((blackRooks & (1L << index)) != 0L) blackRooks &= mask;
            if ((blackQueens & (1L << index)) != 0L) blackQueens &= mask;  // Corrected line for queen
            blackKing &= mask; // Only clear if the king is actually on the square
        }
        updateAggregatedBitboards();
    }


    public PieceType getPieceTypeAtIndex(int index) {
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

    public Color getPieceColorAtIndex(int index) {
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

    public boolean isOccupied(int index) {
        long positionMask = 1L << index;
        return (allPieces & positionMask) != 0;
    }

    public boolean isOccupiedByOpponent(int index, boolean colorWhite) {
        long positionMask = 1L << index;

        if (colorWhite) {
            // Check if the position is occupied by any of the black pieces
            return (blackPieces & positionMask) != 0;
        } else {
            // Check if the position is occupied by any of the white pieces
            return (whitePieces & positionMask) != 0;
        }
    }

    public boolean hasKingNotMoved(boolean whitesTurn) {
        return whitesTurn ? !whiteKingMoved : !blackKingMoved;
    }

    private void markRookAsMoved(int rookIndex) {
        if (rookIndex == 0) {
            whiteRookA1Moved = true;
        } else if (rookIndex == 7) {
            whiteRookH1Moved = true;
        } else if (rookIndex == 56) {
            blackRookA8Moved = true;
        } else if (rookIndex == 63) {
            blackRookH8Moved = true;
        }
    }

    public boolean hasRookMoved(int rookIndex) {
        return switch (rookIndex) {
            case 0 ->  // 'a1'
                    whiteRookA1Moved;
            case 7 ->  // 'h1'
                    whiteRookH1Moved;
            case 56 -> // 'a8'
                    blackRookA8Moved;
            case 63 -> // 'h8'
                    blackRookH8Moved;
            default -> true; // Assume the rook has moved if it's not in one of the starting positions
        };
    }

    public boolean isOccupiedByPawn(int index, boolean whiteColor) {
        // Create a bitmask for the position
        long positionMask = 1L << index;

        // Check if the position is occupied by a pawn of the given color
        if (whiteColor) {
            return (whitePawns & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPawns & positionMask) != 0;
        }
    }

    public boolean isOccupiedByColor(int index, boolean colorWhite) {
        // Convert the position to a bit index
        long positionMask = 1L << index;

        // Check if the position is occupied by a piece of the given color
        if (colorWhite) {
            return (whitePieces & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPieces & positionMask) != 0;
        }
    }

    public boolean isInCheck(boolean whitesTurn) {
        int kingPosition = findKingIndex(whitesTurn);

        if (canQueenAttackKing(kingPosition, whitesTurn)) {
            return true;
        }
        if (canRookAttackKing(kingPosition, whitesTurn)) {
            return true;
        }
        if (canBishopAttackKing(kingPosition, whitesTurn)) {
            return true;
        }
        if (canKnightAttackKing(kingPosition, whitesTurn)) {
            return true;
        }
        if (canPawnAttackKing(kingPosition, whitesTurn)) {
            return true;
        }
        return canKingAttackKing(kingPosition, whitesTurn);
    }

    private int findKingIndex(boolean whitesTurn) {
        // Use bit operations to find the king's position on the board
        // Assuming there's only one king per color on the board.
        long kingBitboard = whitesTurn ? whiteKing : blackKing;
        return Long.numberOfTrailingZeros(kingBitboard);
    }

    // Example for one attack check - similar methods needed for other piece types
    private boolean canPawnAttackKing(int kingIndex, boolean kingColorWhite) {
        int pawnAttackDirection = kingColorWhite ? 1 : -1;
        int kingRank = kingIndex / 8;
        int kingFile = kingIndex % 8;
        boolean pawnColorBlack = !kingColorWhite; // Pawns that can attack must be of opposite color

        int attackRank = kingRank + pawnAttackDirection;

        if (attackRank < 0 || attackRank > 7) {
            return false;
        }

        // Check for pawn attack from the left diagonal
        if (kingFile > 0) {
            int leftAttackIndex = attackRank * 8 + kingFile - 1;
            if (isOccupiedByPawn(leftAttackIndex, pawnColorBlack)) {
                return true;
            }
        }

        // Check for pawn attack from the right diagonal
        if (kingFile < 7) {
            int rightAttackIndex = attackRank * 8 + kingFile + 1;
            return isOccupiedByPawn(rightAttackIndex, pawnColorBlack);
        }

        return false;
    }

    private boolean canKnightAttackKing(int kingIndex, boolean kingColorWhite) {
        long knightAttacks = knightAttackBitmask(kingIndex);
        long opponentKnights = !kingColorWhite ? whiteKnights : blackKnights;
        return (knightAttacks & opponentKnights) != 0;
    }

    private long knightAttackBitmask(int positionIndex) {
        // Define knight move offsets
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        long attacks = 0L;
        int originalFile = positionIndex % 8;
        int originalRank = positionIndex / 8;

        for (int[] move : knightMoves) {
            int file = originalFile + move[0];
            int rank = originalRank + move[1];

            // Check if the move is within the bounds of the chess board
            if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                int targetIndex = rank * 8 + file;
                attacks |= 1L << targetIndex;
            }
        }

        return attacks;
    }

    private boolean canBishopAttackKing(int kingIndex, boolean kingColorWhite) {
        long bishopAttacks = bishopAttackBitmask(kingIndex);

        // Determine the opponent's bishops bitboard based on the king's color
        long opponentBishops = kingColorWhite ? blackBishops : whiteBishops;

        // Check if the opponent has bishops that can attack the king's position
        return (bishopAttacks & opponentBishops) != 0;
    }

    private long bishopAttackBitmask(int positionIndex) {
        long attacks = 0L;
        int originalFile = positionIndex % 8;
        int originalRank = positionIndex / 8;

        // Directions a bishop can move: top-left, top-right, bottom-left, bottom-right
        int[][] directions = {{-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

        for (int[] direction : directions) {
            int targetFile = originalFile + direction[0];
            int targetRank = originalRank + direction[1];

            while (targetFile >= 0 && targetFile < 8 && targetRank >= 0 && targetRank < 8) {
                int targetIndex = targetRank * 8 + targetFile;
                attacks |= 1L << targetIndex;

                // Stop if a piece is encountered
                if ((allPieces & (1L << targetIndex)) != 0) {
                    break;
                }

                targetFile += direction[0];
                targetRank += direction[1];
            }
        }

        return attacks;
    }


    private boolean canRookAttackKing(int kingIndex, boolean kingColorWhite) {
        // Generate the rook attack bitmask from the king's position
        long rookAttacks = rookAttackBitmask(kingIndex);

        // Determine the opponent's rooks bitboard based on the king's color
        long opponentRooks = kingColorWhite ? blackRooks : whiteRooks;

        // Check if the opponent has rooks that can attack the king's position
        return (rookAttacks & opponentRooks) != 0;
    }

    private long rookAttackBitmask(int positionIndex) {
        long attacks = 0L;
        int originalFile = positionIndex % 8;
        int originalRank = positionIndex / 8;

        // Directions a rook can move: up, down, left, right
        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

        for (int[] direction : directions) {
            int targetFile = originalFile + direction[0];
            int targetRank = originalRank + direction[1];

            while (targetFile >= 0 && targetFile < 8 && targetRank >= 0 && targetRank < 8) {
                int targetIndex = targetRank * 8 + targetFile;
                attacks |= 1L << targetIndex;

                if ((allPieces & (1L << targetIndex)) != 0) {
                    break; // Stop if a piece is encountered
                }

                targetFile += direction[0];
                targetRank += direction[1];
            }
        }

        return attacks;
    }


    private boolean canQueenAttackKing(int kingIndex, boolean kingColorWhite) {
        long queenAttacks = queenAttackBitmask(kingIndex);

        // Determine the opponent's queens bitboard based on the king's color
        long opponentQueens = kingColorWhite ? blackQueens : whiteQueens;

        // Check if the opponent has queens that can attack the king's position
        return (queenAttacks & opponentQueens) != 0;
    }

    private long queenAttackBitmask(int positionIndex) {
        // The queen's attack bitmask is a combination of the rook's and bishop's attack bitmasks
        long rookAttacks = rookAttackBitmask(positionIndex);
        long bishopAttacks = bishopAttackBitmask(positionIndex);

        // Combine both attack patterns
        return rookAttacks | bishopAttacks;
    }

    private boolean canKingAttackKing(int kingIndex, boolean kingColorWhite) {
        long kingAttacks = kingAttackBitmask(kingIndex);

        // Determine the opponent's king bitboard based on the king's color
        long opponentKing = kingColorWhite ? blackKing : whiteKing;

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

    public void logBoard() {
        StringBuilder logBoard = new StringBuilder();
        logBoard.append('\n');
        for (int rank = 8; rank >= 1; rank--) {
            for (char file = 'a'; file <= 'h'; file++) {
                int index = bitIndex(file, rank);
                long positionMask = 1L << index;

                // Determine the piece at the current position
                char pieceChar = getPieceChar(positionMask);

                // Add the piece character to the log board
                logBoard.append(pieceChar).append(' ');
            }
            logBoard.append("  ").append(rank).append('\n'); // Append the rank number at the end of each line
        }
        logBoard.append("a b c d e f g h"); // Append file letters at the bottom
        log.info(logBoard.toString()); // Log the current board state
    }

    private char getPieceChar(long positionMask) {
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
        return pieceChar;
    }

    // Helper method to move a piece on a bitboard
    private long moveBit(long pieceBitboard, int fromIndex, int toIndex) {
        // Clear the bit at the fromIndex
        pieceBitboard &= ~(1L << fromIndex);
        // Set the bit at the toIndex
        pieceBitboard |= 1L << toIndex;
        return pieceBitboard;
    }

    private boolean doesMoveWrapAround(int fromIndex, int toIndex) {
        int fromFile = fromIndex % 8;
        int toRank = toIndex / 8;
        int toFile = toIndex % 8;
        // Check if the move wraps around the board horizontally or is outside the board vertically
        return Math.abs(fromFile - toFile) > 1 || toRank < 0 || toRank > 7;
    }

    public void undoMove(int moveInt) {
        int fromIndex = moveInt & 0x3F; // Extract the first 6 bits
        int toIndex = (moveInt >> 6) & 0x3F; // Extract the next 6 bits
        int pieceTypeBits = (moveInt >> 12) & 0x07; // Extract the next 3 bits
        boolean isWhite = (moveInt & (1 << 15)) != 0; // Extract the color bit
        int specialProperty = (moveInt >> 16) & 0x03; // Extract the next 2 bits
        boolean isCapture = (specialProperty & 0x01) != 0;
        boolean isEnPassantMove = specialProperty == 3;
        boolean isCastlingMove = specialProperty == 2;

        int promotionPieceTypeBits = (moveInt >> 18) & 0x07; // Extract the next 3 bits
        int capturedPieceTypeBits = (moveInt >> 21) & 0x07; // Extract the next 3 bits
        boolean isKingFirstMove = (moveInt & (1 << 24)) != 0; // Extract the king's first move bit
        boolean isRookFirstMove = (moveInt & (1 << 25)) != 0; // Extract the rook's first move bit

        // 1. Handle Captured Piece Restoration
        undoCapture(toIndex, capturedPieceTypeBits, isCapture, isWhite, isEnPassantMove);

        // 2. Handle Pawn Promotion
        undoPromotion(promotionPieceTypeBits, fromIndex, toIndex, isWhite);

        // Moving the piece back...
        // Ensure that if the piece is a king, it's handled correctly
        undoPieceMove(pieceTypeBits, fromIndex, toIndex, isWhite);

        // If the move was a castling move, move the rook back
        undoCastling(fromIndex, toIndex, isCastlingMove, isWhite);

        // If the move was a double pawn push, remove the last move double step pawn position
        undoGameState(fromIndex, toIndex, pieceTypeBits, isKingFirstMove, isRookFirstMove, isWhite);

        // Update the aggregated bitboards
        updateAggregatedBitboards();
        whitesTurn = !whitesTurn;
    }

    private void undoGameState(int fromIndex, int toIndex, int pieceTypeBits, boolean isKingFirstMove, boolean isRookFirstMove, boolean isWhite) {

        if (pieceTypeBits == 1 && Math.abs(fromIndex / 8 - toIndex / 8) == 2) {
            lastMoveDoubleStepPawnIndex = -1;
        }

        if (pieceTypeBits == 6 && isKingFirstMove) {
            if (isWhite) {
                whiteKingMoved = false;
                if (isRookFirstMove) {
                    if (toIndex == 6) { // 'g1'
                        whiteRookH1Moved = false;
                    } else if (toIndex == 2) { // 'c1'
                        whiteRookA1Moved = false;
                    }
                }
            } else {
                blackKingMoved = false;
                if (isRookFirstMove) {
                    if (toIndex == 62) { // 'g8'
                        blackRookH8Moved = false;
                    } else if (toIndex == 58) { // 'c8'
                        blackRookA8Moved = false;
                    }
                }
            }
        }

        if (pieceTypeBits == 4 && isRookFirstMove) {
            if (fromIndex == 0) { // 'a1'
                whiteRookA1Moved = false;
            } else if (fromIndex == 7) { // 'h1'
                whiteRookH1Moved = false;
            } else if (fromIndex == 56) { // 'a8'
                blackRookA8Moved = false;
            } else if (fromIndex == 63) { // 'h8'
                blackRookH8Moved = false;
            }
        }
    }

    private void undoCastling(int fromIndex, int toIndex, boolean isCastling, boolean isWhite) {
        if (isCastling) {
            // Determine if this is kingside or queenside castling
            boolean kingside = toIndex > fromIndex;
            int rookFromIndex, rookToIndex;
            if (isWhite) {
                whiteKingHasCastled = false;
            } else {
                blackKingHasCastled = false;
            }
            if (kingside) {
                rookToIndex = isWhite ? 7 : 63;
                rookFromIndex = rookToIndex - 2;
            } else {
                rookToIndex = isWhite ? 0 : 56;
                rookFromIndex = rookToIndex + 3;
            }
            // Move the rook back
            long rookBitboard = intToPiecesBitboard(4, isWhite);
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(4, isWhite, rookBitboard);
        }
    }

    private void undoPieceMove(int pieceTypeBits, int fromIndex, int toIndex, boolean isWhite) {
        long pieceBitboard = intToPiecesBitboard(pieceTypeBits, isWhite);
        pieceBitboard = moveBit(pieceBitboard, toIndex, fromIndex);
        setBitboardForPiece(pieceTypeBits, isWhite, pieceBitboard);
    }

    private void undoPromotion(int promotionPieceTypeBits, int fromIndex, int toIndex, boolean isWhite) {
        if (promotionPieceTypeBits != 0) {
            long promotedPieceBitboard = intToPiecesBitboard(promotionPieceTypeBits, isWhite);
            // Remove promoted piece
            promotedPieceBitboard &= ~(1L << toIndex);
            setBitboardForPiece(promotionPieceTypeBits, isWhite, promotedPieceBitboard);

            // Re-add the pawn
            long pawnBitboard = intToPiecesBitboard(1, isWhite);
            pawnBitboard |= 1L << fromIndex;
            setBitboardForPiece(1, isWhite, pawnBitboard);
        }
    }

    private void undoCapture(int toIndex, int capturedPieceTypeBits, boolean isCapture, boolean isWhite, boolean isEnPassant) {
        if (isCapture) {
            boolean isEnPassantWhite = isEnPassant && isWhite;
            boolean isEnPassantBlack = isEnPassant && !isWhite;

            int enPassantModifier = 0;

            if (isEnPassantWhite) {
                enPassantModifier = -8;
                lastMoveDoubleStepPawnIndex = toIndex + enPassantModifier;
            }
            if (isEnPassantBlack) {
                enPassantModifier = 8;
                lastMoveDoubleStepPawnIndex = toIndex + enPassantModifier;
            }

            long capturedPieceBitboard = intToPiecesBitboard(capturedPieceTypeBits, !isWhite);
            // Restore the captured piece on its bitboard
            capturedPieceBitboard |= (1L << toIndex + enPassantModifier);
            setBitboardForPiece(capturedPieceTypeBits, !isWhite, capturedPieceBitboard);
        }
    }

    private int createMoveInt(int fromIndex, int toIndex, PieceType pieceType, boolean isWhite, boolean isCapture, boolean isCastlingMove, boolean isEnPassantMove, PieceType promotionPieceType, PieceType capturedPieceType, boolean isKingFirstMove, boolean isRookFirstMove) {
        int moveInt = 0;
        moveInt |= fromIndex; // 6 bits for 'from' position
        moveInt |= toIndex << 6; // 6 bits for 'to' position, shifted by 6 bits

        int pieceTypeBits = pieceTypeToInt(pieceType); // 3 bits for piece type
        moveInt |= pieceTypeBits << 12; // Shifted by 12 bits

        if (isWhite) {
            moveInt |= 1 << 15; // 1 bit for color, shifted by 15 bits
        }

        int specialProperty = (isCapture ? 1 : 0) | (isCastlingMove ? 2 : 0) | (isEnPassantMove ? 3 : 0);
        moveInt |= specialProperty << 16; // Shifted by 16 bits

        if (promotionPieceType != null) {
            moveInt |= pieceTypeToInt(promotionPieceType) << 18; // 3 bits for promotion piece type, shifted by 18 bits
        } else {
            moveInt &= ~(0x07 << 18); // Ensure promotion bits are set to 000 if no promotion
        }

        if (capturedPieceType != null) {
            moveInt |= pieceTypeToInt(capturedPieceType) << 21; // 3 bits for captured piece type, shifted by 21 bits
        } else {
            moveInt &= ~(0x07 << 21); // Ensure captured piece bits are set to 000 if no piece is captured
        }

        if (isKingFirstMove) {
            moveInt |= 1 << 24; // 1 bit for king's first move, shifted by 24 bits
        }

        if (isRookFirstMove) {
            moveInt |= 1 << 25; // 1 bit for rook's first move, shifted by 25 bits
        }

        return moveInt;
    }

    private int pieceTypeToInt(PieceType pieceType) {
        return switch (pieceType) {
            case PAWN -> 1;
            case KNIGHT -> 2;
            case BISHOP -> 3;
            case ROOK -> 4;
            case QUEEN -> 5;
            case KING -> 6;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitBoard bitBoard = (BitBoard) o;
        return whitePawns == bitBoard.whitePawns &&
                blackPawns == bitBoard.blackPawns &&
                whiteKnights == bitBoard.whiteKnights &&
                blackKnights == bitBoard.blackKnights &&
                whiteBishops == bitBoard.whiteBishops &&
                blackBishops == bitBoard.blackBishops &&
                whiteRooks == bitBoard.whiteRooks &&
                blackRooks == bitBoard.blackRooks &&
                whiteQueens == bitBoard.whiteQueens &&
                blackQueens == bitBoard.blackQueens &&
                whiteKing == bitBoard.whiteKing &&
                blackKing == bitBoard.blackKing &&
                whitePieces == bitBoard.whitePieces &&
                blackPieces == bitBoard.blackPieces &&
                allPieces == bitBoard.allPieces &&
                whiteKingMoved == bitBoard.whiteKingMoved &&
                blackKingMoved == bitBoard.blackKingMoved &&
                whiteRookA1Moved == bitBoard.whiteRookA1Moved &&
                whiteRookH1Moved == bitBoard.whiteRookH1Moved &&
                blackRookA8Moved == bitBoard.blackRookA8Moved &&
                blackRookH8Moved == bitBoard.blackRookH8Moved;
    }

    @Override
    public int hashCode() {
        return Objects.hash(whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops,
                whiteRooks, blackRooks, whiteQueens, blackQueens, whiteKing, blackKing,
                whitePieces, blackPieces, allPieces, whiteKingMoved, blackKingMoved,
                whiteRookA1Moved, whiteRookH1Moved, blackRookA8Moved, blackRookH8Moved,
                lastMoveDoubleStepPawnIndex);
    }

    private PieceType getPieceTypeAtSquare(int square) {

        return getPieceTypeAtIndex(square);
    }

    private Color getPieceColorAtSquare(int square) {
        return getPieceColorAtIndex(square);
    }

    private int getPieceIndex(PieceType pieceType, Color pieceColor) {
        int index = pieceType.ordinal() * 2; // There are two colors for each piece type
        if (pieceColor == Color.BLACK) {
            index++; // Add 1 for black pieces
        }
        return index;
    }

    public long getBoardStateHash() {
        long hash = 0;

        // Iterate over all squares and XOR the hash with the piece hash values
        for (int square = 0; square < 64; square++) {
            PieceType pieceType = getPieceTypeAtSquare(square);
            Color pieceColor = getPieceColorAtSquare(square);
            if (pieceType != null && pieceColor != null) {
                int pieceIndex = getPieceIndex(pieceType, pieceColor); // You need to implement this method
                hash ^= ZobristTable.getPieceSquareHash(pieceIndex, square);
            }
        }

        // Include castling rights in the hash
        if (!whiteKingMoved) {
            if (!whiteRookH1Moved) {
                hash ^= ZobristTable.getCastlingRightsHash(0); // White Kingside
            }
            if (!whiteRookA1Moved) {
                hash ^= ZobristTable.getCastlingRightsHash(1); // White Queenside
            }
        }
        if (!blackKingMoved) {
            if (!blackRookH8Moved) {
                hash ^= ZobristTable.getCastlingRightsHash(2); // Black Kingside
            }
            if (!blackRookA8Moved) {
                hash ^= ZobristTable.getCastlingRightsHash(3); // Black Queenside
            }
        }
        //Commented because led to an error check uncomment and start Test checkForEngine() in BitBoardTest.java
        /*        // Include en passant square in the hash
        if (lastMoveDoubleStepPawnPosition != null) {
            int file = lastMoveDoubleStepPawnPosition.getX() - 'a';
            hash ^= ZobristTable.getEnPassantSquareHash(file);
        }*/

        // Include the player's turn in the hash
        if (whitesTurn) {
            hash ^= ZobristTable.getBlackTurnHash(); // XOR with blackTurnHash means it's white's turn
        }

        return hash;
    }

}

