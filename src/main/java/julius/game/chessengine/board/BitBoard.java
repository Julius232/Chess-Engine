package julius.game.chessengine.board;

import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.helper.BishopHelper;
import julius.game.chessengine.helper.PawnHelper;
import julius.game.chessengine.helper.ZobristTable;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static julius.game.chessengine.helper.BishopHelper.BISHOP_POSITIONAL_VALUES;
import static julius.game.chessengine.helper.BitHelper.*;
import static julius.game.chessengine.helper.KnightHelper.KNIGHT_POSITIONAL_VALUES;
import static julius.game.chessengine.helper.KnightHelper.knightMoves;
import static julius.game.chessengine.helper.PawnHelper.BLACK_PAWN_POSITIONAL_VALUES;
import static julius.game.chessengine.helper.PawnHelper.countCenterPawns;
import static julius.game.chessengine.helper.QueenHelper.QUEEN_POSITIONAL_VALUES;

@Log4j2
public class BitBoard {

    BishopHelper bishopHelper = BishopHelper.getInstance();

    // Constants for the initial positions of each piece type
    private static final long INITIAL_WHITE_PAWN_POSITION = 0x000000000000FF00L; // Pawns on the second rank (a2-h2)
    private static final long INITIAL_BLACK_PAWN_POSITION = 0x00FF000000000000L; // Pawns on the seventh rank (a7-h7)
    private static final long INITIAL_WHITE_KNIGHT_POSITION = 0x0000000000000042L; // Knights on b1 and g1
    private static final long INITIAL_BLACK_KNIGHT_POSITION = 0x4200000000000000L; // Knights on b8 and g8
    private static final long INITIAL_WHITE_BISHOP_POSITION = 0x0000000000000024L; // Bishops on c1 and f1
    private static final long INITIAL_BLACK_BISHOP_POSITION = 0x2400000000000000L; // Bishops on c8 and f8
    private static final long INITIAL_WHITE_ROOK_POSITION = 0x0000000000000081L; // Rooks on a1 and h1
    private static final long INITIAL_BLACK_ROOK_POSITION = 0x8100000000000000L; // Rooks on a8 and h8
    private static final long INITIAL_WHITE_QUEEN_POSITION = 0x0000000000000008L; // Queen on d1
    private static final long INITIAL_BLACK_QUEEN_POSITION = 0x0800000000000000L; // Queen on d8
    private static final long INITIAL_WHITE_KING_POSITION = 0x0000000000000010L; // King on e1
    private static final long INITIAL_BLACK_KING_POSITION = 0x1000000000000000L; // King on e8


    public boolean whitesTurn = true;
    // Add score field to the BitBoard class
    private Score currentScore;
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
    @Getter
    private Position lastMoveDoubleStepPawnPosition;

    // Flags to track if the king and rooks have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookA1Moved = false;
    private boolean whiteRookH1Moved = false;
    private boolean blackRookA8Moved = false;
    private boolean blackRookH8Moved = false;

    public BitBoard(boolean whitesTurn, long whitePawns, long blackPawns, long whiteKnights, long blackKnights, long whiteBishops, long blackBishops, long whiteRooks, long blackRooks, long whiteQueens, long blackQueens, long whiteKing, long blackKing, long whitePieces, long blackPieces, long allPieces, Position lastMoveDoubleStepPawnPosition, boolean whiteKingMoved, boolean blackKingMoved, boolean whiteRookA1Moved, boolean whiteRookH1Moved, boolean blackRookA8Moved, boolean blackRookH8Moved) {
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
        this.lastMoveDoubleStepPawnPosition = lastMoveDoubleStepPawnPosition;
        this.whiteKingMoved = whiteKingMoved;
        this.blackKingMoved = blackKingMoved;
        this.whiteRookA1Moved = whiteRookA1Moved;
        this.whiteRookH1Moved = whiteRookH1Moved;
        this.blackRookA8Moved = blackRookA8Moved;
        this.blackRookH8Moved = blackRookH8Moved;
        updateScore();
    }

    public BitBoard() {
        updateScore();
        setInitialPosition();
    }

    public BitBoard(BitBoard other) {
        // Copying all the long fields representing the pieces
        this.currentScore = new Score(other.getScore().getScoreWhite(), other.getScore().getScoreBlack());
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

        this.lastMoveDoubleStepPawnPosition = other.lastMoveDoubleStepPawnPosition != null ? new Position(other.lastMoveDoubleStepPawnPosition) : null;
    }

    public void updateScore() {
        // Initialize bonuses and penalties
        int whiteCenterBonus = 0;
        int blackCenterBonus = 0;
        int whiteDoubledPenalty = 0;
        int blackDoubledPenalty = 0;
        int whiteIsolatedPenalty = 0;
        int blackIsolatedPenalty = 0;

        // Define the piece values
        final int PAWN_VALUE = 100;   // Pawns are worth 1 point, scaled by 100
        final int KNIGHT_VALUE = 300; // Knights are worth 3 points
        final int BISHOP_VALUE = 300; // Bishops are worth 3 points
        final int ROOK_VALUE = 500;   // Rooks are worth 5 points
        final int QUEEN_VALUE = 9000;  // Queens are worth 9 points

        // Initialize scores
        int whiteScore = 0;
        int blackScore = 0;

        final int CENTER_PAWN_BONUS = 10;   // Bonus points for pawns in the center
        final int DOUBLED_PAWN_PENALTY = -20; // Penalty points for doubled pawns
        final int ISOLATED_PAWN_PENALTY = -10; // Penalty points for isolated pawns

        final int START_POSITION_PENALTY = -50; // Define the penalty value for starting position

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
        // Define additional score values

        // Calculate bonuses and penalties for white
        whiteCenterBonus += countCenterPawns(whitePawns) * CENTER_PAWN_BONUS;
        whiteDoubledPenalty += countDoubledPawns(whitePawns) * DOUBLED_PAWN_PENALTY;
        whiteIsolatedPenalty += countIsolatedPawns(whitePawns) * ISOLATED_PAWN_PENALTY;

        // Calculate bonuses and penalties for black
        blackCenterBonus += countCenterPawns(blackPawns) * CENTER_PAWN_BONUS;
        blackDoubledPenalty += countDoubledPawns(blackPawns) * DOUBLED_PAWN_PENALTY;
        blackIsolatedPenalty += countIsolatedPawns(blackPawns) * ISOLATED_PAWN_PENALTY;

        // Apply bonuses and penalties to the score
        whiteScore += whiteCenterBonus + whiteDoubledPenalty + whiteIsolatedPenalty;
        blackScore += blackCenterBonus + blackDoubledPenalty + blackIsolatedPenalty;

        // Apply positional values to the pawns
        whiteScore += applyPositionalValues(whitePawns, PawnHelper.WHITE_PAWN_POSITIONAL_VALUES);
        blackScore += applyPositionalValues(blackPawns, BLACK_PAWN_POSITIONAL_VALUES);

        // Calculate positional values for white and black knights
        whiteScore += applyPositionalValues(whiteKnights, KNIGHT_POSITIONAL_VALUES);
        blackScore += applyPositionalValues(blackKnights, KNIGHT_POSITIONAL_VALUES);

        // Calculate positional values for white and black knights
        whiteScore += applyPositionalValues(whiteBishops, BISHOP_POSITIONAL_VALUES);
        blackScore += applyPositionalValues(blackBishops, BISHOP_POSITIONAL_VALUES);

        whiteScore += applyPositionalValues(whiteQueens, QUEEN_POSITIONAL_VALUES);
        blackScore += applyPositionalValues(blackQueens, QUEEN_POSITIONAL_VALUES);

        // Check if white pieces are all on starting squares
        if (areAllPiecesOnStartingSquares(whiteKnights, whiteBishops, whiteRooks, true)) {
            whiteScore += START_POSITION_PENALTY;
        }
        // Check if black pieces are all on starting squares
        if (areAllPiecesOnStartingSquares(blackKnights, blackBishops, blackRooks, false)) {
            blackScore += START_POSITION_PENALTY;
        }

        // Return the score encapsulated in a Score object
        this.currentScore = new Score(whiteScore, blackScore);
    }

    private boolean areAllPiecesOnStartingSquares(long knights, long bishops, long rooks, boolean isWhite) {
        if (isWhite) {
            return (knights == INITIAL_WHITE_KNIGHT_POSITION ||
                    bishops == INITIAL_WHITE_BISHOP_POSITION ||
                    rooks == INITIAL_WHITE_ROOK_POSITION);
        } else {
            return (knights == INITIAL_BLACK_KNIGHT_POSITION ||
                    bishops == INITIAL_BLACK_BISHOP_POSITION ||
                    rooks == INITIAL_BLACK_ROOK_POSITION);
        }
    }

    public List<Move> getAllCurrentPossibleMoves() {
        return generateAllPossibleMoves(whitesTurn);
    }

    private int applyPositionalValues(long bitboard, int[] positionalValues) {
        int score = 0;
        for (int i = Long.numberOfTrailingZeros(bitboard); i < 64 - Long.numberOfLeadingZeros(bitboard); i++) {
            if (((1L << i) & bitboard) != 0) {
                score += positionalValues[i];
            }
        }
        return score;
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
    }

    // Method to get the bitboard for a specific piece type and color
    public long getBitboardForPiece(PieceType piece, boolean isWhite) {
        if (isWhite) {
            return switch (piece) {
                case PAWN -> whitePawns;
                case KNIGHT -> whiteKnights;
                case BISHOP -> whiteBishops;
                case ROOK -> whiteRooks;
                case QUEEN -> whiteQueens;
                case KING -> whiteKing;
            };
        } else {
            return switch (piece) {
                case PAWN -> blackPawns;
                case KNIGHT -> blackKnights;
                case BISHOP -> blackBishops;
                case ROOK -> blackRooks;
                case QUEEN -> blackQueens;
                case KING -> blackKing;
            };
        }
    }

    // Call these methods within the movePiece method when a king or rook moves
    private void markKingAsMoved(boolean isWhite) {
        if (isWhite) {
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

    public List<Move> generateAllPossibleMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();

        // Generate moves for each piece type
        moves.addAll(generatePawnMoves(whitesTurn));
        moves.addAll(generateKnightMoves(whitesTurn));
        moves.addAll(generateBishopMoves(whitesTurn));
        moves.addAll(generateRookMoves(whitesTurn));
        moves.addAll(generateQueenMoves(whitesTurn));
        moves.addAll(generateKingMoves(whitesTurn));

        return moves;
    }

    // Method to set the bitboard for a specific piece type and color
    private void setBitboardForPiece(PieceType piece, boolean isWhite, long bitboard) {
        if (isWhite) {
            switch (piece) {
                case PAWN -> whitePawns = bitboard;
                case KNIGHT -> whiteKnights = bitboard;
                case BISHOP -> whiteBishops = bitboard;
                case ROOK -> whiteRooks = bitboard;
                case QUEEN -> whiteQueens = bitboard;
                case KING -> whiteKing = bitboard;
                default -> throw new IllegalArgumentException("Unknown piece type: " + piece);
            }

        } else {
            switch (piece) {
                case PAWN -> blackPawns = bitboard;
                case KNIGHT -> blackKnights = bitboard;
                case BISHOP -> blackBishops = bitboard;
                case ROOK -> blackRooks = bitboard;
                case QUEEN -> blackQueens = bitboard;
                case KING -> blackKing = bitboard;
                default -> throw new IllegalArgumentException("Unknown piece type: " + piece);
            }
        }

        // After setting the bitboard, update the aggregated bitboards
        updateAggregatedBitboards();
    }

    void updateAggregatedBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
    }

    private List<Move> generatePawnMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();
        long pawns = whitesTurn ? whitePawns : blackPawns;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;
        long emptySquares = ~(whitePieces | blackPieces);

        long singleStepForward = whitesTurn ? pawns << 8 : pawns >>> 8;
        singleStepForward &= emptySquares;

        long doubleStepForward = 0L;
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

        if (lastMoveDoubleStepPawnPosition != null) {
            generateEnPassantMoves(moves, pawns, whitesTurn);
        }

        return moves;
    }

    private void generateEnPassantMoves(List<Move> moves, long pawns, boolean whitesTurn) {
        int enPassantRank = whitesTurn ? 5 : 2;
        int fileIndexOfDoubleSteppedPawn = lastMoveDoubleStepPawnPosition.getX() - 'a';
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


    private void addEnPassantMove(List<Move> moves, int fromIndex, int toIndex, boolean whitesTurn) {
        Position fromPosition = indexToPosition(fromIndex);
        Position toPosition = indexToPosition(toIndex);
        moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, true, false, true, null, PieceType.PAWN, false, false));
    }

    private void addPawnMoves(List<Move> moves, long bitboard, int shift, boolean isCapture, boolean whitesTurn) {
        int direction = whitesTurn ? 1 : -1;

        while (bitboard != 0) {
            int toIndex = Long.numberOfTrailingZeros(bitboard);
            int fromIndex = whitesTurn ? toIndex - shift : toIndex + shift;
            Position fromPosition = indexToPosition(fromIndex);
            Position toPosition = indexToPosition(toIndex);
            boolean isPromotion = whitesTurn ? toPosition.getY() == 8 : toPosition.getY() == 1;

            PieceType capturedType = isCapture ? getPieceTypeAtPosition(toPosition) : null;

            // Calculate the differences in the x and y coordinates
            int yDiff = (toPosition.getY() - fromPosition.getY()) * direction; // Multiplied by direction for forward movement

            if (checkForInitialDoubleSquareMove(direction, fromPosition, toPosition, yDiff)) {
                if (isPromotion) {
                    moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.QUEEN, capturedType, false, false));
                    moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.ROOK, capturedType, false, false));
                    moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.BISHOP, capturedType, false, false));
                    moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, isCapture, false, false, PieceType.KNIGHT, capturedType, false, false));
                } else {
                    moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, whitesTurn, isCapture, false, false, null, capturedType, false, false));
                }
            }

            bitboard &= bitboard - 1; // Clear the processed bit
        }
    }

    private boolean checkForInitialDoubleSquareMove(int direction, Position fromPosition, Position toPosition, int yDiff) {
        return !(yDiff == 2 && (isOccupied(toPosition) | isOccupied(new Position(fromPosition.getX(), fromPosition.getY() + direction))));
    }

    private List<Move> generateKnightMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();
        long knights = whitesTurn ? whiteKnights : blackKnights;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;

        // Iterate over all bits where knights exist
        for (int i = Long.numberOfTrailingZeros(knights); i < 64 - Long.numberOfLeadingZeros(knights); i++) {
            if (((1L << i) & knights) != 0) {
                int fromRank = i / 8;
                int fromFile = i % 8;
                Position fromPosition = indexToPosition(i);

                // Iterate through all possible L moves
                for (int[] offset : knightMoves) {
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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                            moves.add(new Move(fromPosition, toPosition, PieceType.KNIGHT, whitesTurn, isCapture, false, false, null, capturedPieceType, false, false));
                        }
                    }
                }
            }
        }

        return moves;
    }


    private List<Move> generateBishopMoves(boolean isWhite) {
        List<Move> moves = new ArrayList<>();
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
                moves.add(new Move(indexToPosition(bishopSquare), indexToPosition(targetSquare), PieceType.BISHOP, isWhite, isCapture, false, false, null, isCapture ? getPieceTypeAtPosition(indexToPosition(targetSquare)) : null, false, false));
            }
        }

        return moves;
    }


    private List<Move> generateRookMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();
        long rooks = whitesTurn ? whiteRooks : blackRooks;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;

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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;

                            boolean isFirstRookMove = !hasRookMoved(fromPosition);

                            moves.add(new Move(fromPosition, toPosition, PieceType.ROOK, whitesTurn, isCapture, false, false, null, capturedPieceType, false, isFirstRookMove));

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


    private List<Move> generateQueenMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();
        long queens = whitesTurn ? whiteQueens : blackQueens;
        long ownPieces = whitesTurn ? whitePieces : blackPieces;
        long opponentPieces = whitesTurn ? blackPieces : whitePieces;

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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                            moves.add(new Move(fromPosition, toPosition, PieceType.QUEEN, whitesTurn, isCapture, false, false, null, capturedPieceType, false, false));

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

    private List<Move> generateKingMoves(boolean whitesTurn) {
        List<Move> moves = new ArrayList<>();
        long kingBitboard = whitesTurn ? whiteKing : blackKing;
        int kingPositionIndex = Long.numberOfTrailingZeros(kingBitboard);
        Position kingPosition = indexToPosition(kingPositionIndex);

        // Offsets for a king's normal moves
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};

        boolean isFirstKingMove = hasKingNotMoved(whitesTurn);

        for (int offset : offsets) {
            int targetIndex = kingPositionIndex + offset;
            // Check if the move is within board bounds and does not wrap around
            if (targetIndex >= 0 && targetIndex < 64 && !doesMoveWrapAround(kingPositionIndex, targetIndex)) {
                Position toPosition = indexToPosition(targetIndex);
                if (!isOccupiedByColor(toPosition, whitesTurn)) {
                    boolean isCapture = isOccupiedByOpponent(toPosition, whitesTurn);
                    PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                    moves.add(new Move(kingPosition, toPosition, PieceType.KING, whitesTurn, isCapture, false, false, null, capturedPieceType, isFirstKingMove, false));
                }
            }
        }

        // Castling logic
        // The actual castling checks should be implemented in the canCastleKingside and canCastleQueenside methods
        if (canKingCastle(whitesTurn)) {
            // Kingside castling
            if (canCastleKingside(whitesTurn, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex + 2), PieceType.KING, whitesTurn, false, true, false, null, null, true, true));
            }
            // Queenside castling
            if (canCastleQueenside(whitesTurn, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex - 2), PieceType.KING, whitesTurn, false, true, false, null, null, true, true));
            }
        }

        return moves;
    }

    private boolean canKingCastle(boolean whitesTurn) {
        // The king must not have moved and must not be in check
        return hasKingNotMoved(whitesTurn) && !isInCheck(whitesTurn);
    }

    private boolean canCastleKingside(boolean colorWhite, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] kingsideSquares = {kingPositionIndex + 1, kingPositionIndex + 2};
        for (int square : kingsideSquares) {
            if (isOccupied(indexToPosition(square)) || isSquareUnderAttack(indexToPosition(square), colorWhite)) {
                return false;
            }
        }

        // Check if the rook has moved
        if (hasRookMoved(new Position('h', colorWhite ? 1 : 8))) {
            return false;
        }

        // Check if the rook still exists
        Position rookPosition = new Position('h', colorWhite ? 1 : 8);
        return isRookAtPosition(rookPosition);
    }

    private boolean canCastleQueenside(boolean colorWhite, int kingPositionIndex) {
        // Ensure the squares between the king and the rook are unoccupied and not under attack
        int[] queensideSquares = {kingPositionIndex - 1, kingPositionIndex - 2, kingPositionIndex - 3};
        for (int square : queensideSquares) {
            if (isOccupied(indexToPosition(square)) || (square != kingPositionIndex - 3 && isSquareUnderAttack(indexToPosition(square), colorWhite))) {
                return false;
            }
        }
        if (hasRookMoved(new Position('a', colorWhite ? 1 : 8))) {
            return false;
        }
        Position rookPosition = new Position('a', colorWhite ? 1 : 8);
        return isRookAtPosition(rookPosition);
    }

    private boolean isRookAtPosition(Position position) {
        PieceType pieceAtPosition = getPieceTypeAtPosition(position);
        return pieceAtPosition == PieceType.ROOK;
    }

    private boolean isSquareUnderAttack(Position position, boolean color) {
        boolean opponentColor = !color;
        return canPawnAttackPosition(position, opponentColor) ||
                canKnightAttackPosition(position, opponentColor) ||
                canBishopAttackPosition(position, opponentColor) ||
                canRookAttackPosition(position, opponentColor) ||
                canQueenAttackPosition(position, opponentColor) ||
                canKingAttackPosition(position, opponentColor);
    }

    private boolean canPawnAttackPosition(Position position, boolean colorWhite) {
        // Pawns attack diagonally, so check the two squares from which an opponent pawn could attack
        int direction = colorWhite ? -1 : 1; // Pawns move up if white, down if black
        Position attackFromLeft = new Position((char) (position.getX() - 1), position.getY() + direction);
        Position attackFromRight = new Position((char) (position.getX() + 1), position.getY() + direction);
        return (isValidBoardPosition(attackFromLeft) && isOccupiedByPawn(attackFromLeft, colorWhite)) ||
                (isValidBoardPosition(attackFromRight) && isOccupiedByPawn(attackFromRight, colorWhite));
    }

// Similar methods would be implemented for knights, bishops, rooks, queens, and kings.
// These methods would use the attack patterns for each piece type to determine if they
// could possibly attack the given square. For example, a knight's attack pattern is an L-shape,
// a bishop attacks along diagonals, etc.

    // Example for knights:
    private boolean canKnightAttackPosition(Position position, boolean color) {
        long knightsBitboard = color ? whiteKnights : blackKnights;
        long knightAttacks = knightAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (knightAttacks & knightsBitboard) != 0;
    }

// The knightAttackBitmask method would generate all the potential squares a knight can attack from a given position.
// It would look similar to the previously provided knightAttackBitmask method but would center around the target position instead.

    private boolean canBishopAttackPosition(Position position, boolean colorWhite) {
        long bishopsBitboard = colorWhite ? whiteBishops : blackBishops;
        long bishopAttacks = bishopAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (bishopAttacks & bishopsBitboard) != 0;
    }

    private boolean canRookAttackPosition(Position position, boolean colorWhite) {
        long rooksBitboard = colorWhite ? whiteRooks : blackRooks;
        long rookAttacks = rookAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (rookAttacks & rooksBitboard) != 0;
    }

    private boolean canQueenAttackPosition(Position position, boolean colorWhite) {
        long queensBitboard = colorWhite ? whiteQueens : blackQueens;
        long queenAttacks = queenAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (queenAttacks & queensBitboard) != 0;
    }

    private boolean canKingAttackPosition(Position position, boolean colorWhite) {
        long kingsBitboard = colorWhite ? whiteKing : blackKing;
        long kingAttacks = kingAttackBitmask(bitIndex(position.getX(), position.getY()));
        return (kingAttacks & kingsBitboard) != 0;
    }

// The above-mentioned bishopAttackBitmask, rookAttackBitmask, queenAttackBitmask, and kingAttackBitmask methods
// would generate all potential squares each piece can attack from a given position and compare it to the current
// bitboard of that piece type to see if there's an overlap.

    public void performMove(Move move, boolean scoreNeedsUpdate) {
        int fromIndex = bitIndex(move.getFrom().getX(), move.getFrom().getY());
        int toIndex = bitIndex(move.getTo().getX(), move.getTo().getY());

        long pieceBitboard = getBitboardForPiece(move.getPieceType(), move.isColorWhite());

        if (move.isCapture()) {
            clearSquare(toIndex, !move.isColorWhite());
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
            long rookBitboard = getBitboardForPiece(PieceType.ROOK, move.isColorWhite());
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(PieceType.ROOK, move.isColorWhite(), rookBitboard);

            // Mark the rook as moved
            markRookAsMoved(rookFromPosition);
        }

        // Move the piece
        pieceBitboard = moveBit(pieceBitboard, fromIndex, toIndex);
        setBitboardForPiece(move.getPieceType(), move.isColorWhite(), pieceBitboard);

        if (move.isPromotionMove()) {
            // Clear the pawn from the promotion square
            clearSquare(toIndex, move.isColorWhite());

            // Set the bitboard for the promotion piece
            long promotionPieceBitboard = getBitboardForPiece(move.getPromotionPieceType(), move.isColorWhite());
            promotionPieceBitboard |= (1L << toIndex); // Place the promotion piece on the promotion square
            setBitboardForPiece(move.getPromotionPieceType(), move.isColorWhite(), promotionPieceBitboard);
        }

        // Mark the king as moved if it was a king move
        if (move.getPieceType() == PieceType.KING) {
            markKingAsMoved(move.isColorWhite());
        }

        if (move.getPieceType() == PieceType.ROOK) {
            markRookAsMoved(indexToPosition(fromIndex));
        }

        if (move.getPieceType() == PieceType.PAWN && Math.abs(move.getFrom().getY() - move.getTo().getY()) == 2) {
            lastMoveDoubleStepPawnPosition = move.getTo();
        } else {
            lastMoveDoubleStepPawnPosition = null;
        }

        if (move.isEnPassantMove()) {
            // Clear the captured pawn from its position for en passant
            clearSquare(bitIndex(move.getTo().getX(), move.getFrom().getY()), !move.isColorWhite());
        }

        updateAggregatedBitboards();
        whitesTurn = !whitesTurn;

        if (scoreNeedsUpdate) {
            updateScore();
        }
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

    public boolean isOccupiedByOpponent(Position position, boolean colorWhite) {
        int index = bitIndex(position.getX(), position.getY());
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

    public boolean isOccupiedByPawn(Position position, boolean whiteColor) {
        // Convert the position to the bit index
        int index = bitIndex(position.getX(), position.getY());

        // Create a bitmask for the position
        long positionMask = 1L << index;

        // Check if the position is occupied by a pawn of the given color
        if (whiteColor) {
            return (whitePawns & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPawns & positionMask) != 0;
        }
    }

    public boolean isOccupiedByColor(Position position, boolean colorWhite) {
        // Convert the position to a bit index
        int index = bitIndex(position.getX(), position.getY());
        long positionMask = 1L << index;

        // Check if the position is occupied by a piece of the given color
        if (colorWhite) {
            return (whitePieces & positionMask) != 0;
        } else { // Color.BLACK
            return (blackPieces & positionMask) != 0;
        }
    }

    private boolean isValidBoardPosition(Position position) {
        return position.getX() >= 'a' && position.getX() <= 'h' && position.getY() >= 1 && position.getY() <= 8;
    }

    private Color getOpponentColor(Color color) {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public boolean isInCheck(boolean whitesTurn) {
        Position kingPosition = findKingPosition(whitesTurn);

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

    private Position findKingPosition(boolean whitesTurn) {
        // Use bit operations to find the king's position on the board
        // Assuming there's only one king per color on the board.
        long kingBitboard = whitesTurn ? whiteKing : blackKing;
        int index = Long.numberOfTrailingZeros(kingBitboard);
        return indexToPosition(index);
    }

    // Example for one attack check - similar methods needed for other piece types
    private boolean canPawnAttackKing(Position kingPosition, boolean kingColorWhite) {
        int pawnAttackDirection = kingColorWhite ? 1 : -1;
        int attackRank = kingPosition.getY() + pawnAttackDirection;
        char kingFile = kingPosition.getX();
        boolean black = !kingColorWhite;

        // Check left attack
        if (kingFile > 'a') {
            if (isOccupiedByPawn(new Position((char) (kingFile - 1), attackRank), black)) {
                return true;
            }
        }

        // Check right attack
        if (kingFile < 'h') {
            return isOccupiedByPawn(new Position((char) (kingFile + 1), attackRank), black);
        }

        return false;
    }

    private boolean canKnightAttackKing(Position kingPosition, boolean kingColorWhite) {
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());
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

    private boolean canBishopAttackKing(Position kingPosition, boolean kingColorWhite) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the bishop attack bitmask from the king's position
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


    private boolean canRookAttackKing(Position kingPosition, boolean kingColorWhite) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

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


    private boolean canQueenAttackKing(Position kingPosition, boolean kingColorWhite) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the queen attack bitmask from the king's position
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

    private boolean canKingAttackKing(Position kingPosition, boolean kingColorWhite) {
        // Calculate the bit index of the king's position
        int kingIndex = bitIndex(kingPosition.getX(), kingPosition.getY());

        // Generate the king attack bitmask from the king's position
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

    public Score getScore() {
        return this.currentScore;
    }

    // Method to count pawns in the center (e4, d4, e5, d5 squares)


    // Method to count doubled pawns, which are two pawns of the same color on the same file
    private int countDoubledPawns(long pawnsBitboard) {
        int doubledPawns = 0;
        for (char file = 'a'; file <= 'h'; file++) {
            long fileBitboard = fileBitboard(file);
            if (Long.bitCount(pawnsBitboard & fileBitboard) > 1) {
                doubledPawns++;
            }
        }
        return doubledPawns;
    }

    // Helper method to get a bitboard representing a file
    private long fileBitboard(char file) {
        long fileBitboard = 0L;
        for (int rank = 1; rank <= 8; rank++) {
            fileBitboard |= 1L << bitIndex(file, rank);
        }
        return fileBitboard;
    }

    // Method to count isolated pawns, which are pawns with no friendly pawns on adjacent files
    private int countIsolatedPawns(long pawnsBitboard) {
        int isolatedPawns = 0;
        for (char file = 'a'; file <= 'h'; file++) {
            long fileBitboard = fileBitboard(file);
            long adjacentFiles = (file > 'a' ? fileBitboard((char) (file - 1)) : 0L)
                    | (file < 'h' ? fileBitboard((char) (file + 1)) : 0L);
            if ((pawnsBitboard & fileBitboard) != 0 && (pawnsBitboard & adjacentFiles) == 0) {
                isolatedPawns++;
            }
        }
        return isolatedPawns;
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
        logBoard.append("a b c d e f g h"); // Append file letters at the bottom
        log.info(logBoard.toString()); // Log the current board state
    }

    // Helper method to move a piece on a bitboard
    private long moveBit(long pieceBitboard, int fromIndex, int toIndex) {
        // Clear the bit at the from index
        pieceBitboard &= ~(1L << fromIndex);
        // Set the bit at the to index
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

    public void undoMove(Move move, boolean scoreNeedsUpdate) {
        int fromIndex = bitIndex(move.getFrom().getX(), move.getFrom().getY());
        int toIndex = bitIndex(move.getTo().getX(), move.getTo().getY());

        // 1. Handle Captured Piece Restoration
        undoCapture(move, toIndex);

        // 2. Handle Pawn Promotion
        undoPromotion(move, fromIndex, toIndex);

        // Moving the piece back...
        // Ensure that if the piece is a king, it's handled correctly
        undoPieceMove(move, fromIndex, toIndex);

        // If the move was a castling move, move the rook back
        undoCastling(move);

        // If the move was a double pawn push, remove the last move double step pawn position
        undoGameState(move);

        // Update the aggregated bitboards
        updateAggregatedBitboards();
        if (scoreNeedsUpdate) {
            updateScore();
        }
        whitesTurn = !whitesTurn;
    }

    private void undoGameState(Move move) {
        if (move.getPieceType() == PieceType.PAWN && Math.abs(move.getFrom().getY() - move.getTo().getY()) == 2) {
            lastMoveDoubleStepPawnPosition = null;
        }

        // Restore the state of the king and rook moved flags if necessary
        if (move.getPieceType() == PieceType.KING && move.isKingFirstMove()) {
            if (move.isColorWhite()) {
                whiteKingMoved = false;
                if (move.isRookFirstMove()) {
                    if (move.getTo().getX() == 'g') {
                        whiteRookH1Moved = false;
                    } else {
                        whiteRookA1Moved = false;
                    }
                }
            } else {
                blackKingMoved = false;
                if (move.isRookFirstMove()) {
                    if (move.getTo().getX() == 'g') {
                        blackRookH8Moved = false;
                    } else {
                        blackRookA8Moved = false;
                    }
                }
            }

        }

        // Assuming we also track the moves of the rooks for castling purposes, restore their moved state
        if (move.getPieceType() == PieceType.ROOK && move.isRookFirstMove()) {
            if (move.getFrom().equals(new Position('a', 1))) {
                whiteRookA1Moved = false;
            } else if (move.getFrom().equals(new Position('h', 1))) {
                whiteRookH1Moved = false;
            } else if (move.getFrom().equals(new Position('a', 8))) {
                blackRookA8Moved = false;
            } else if (move.getFrom().equals(new Position('h', 8))) {
                blackRookH8Moved = false;
            }
        }
    }

    private void undoCastling(Move move) {
        if (move.isCastlingMove()) {
            // Determine if this is kingside or queenside castling
            boolean kingside = move.getTo().getX() > move.getFrom().getX();
            Position rookFromPosition, rookToPosition;
            if (kingside) {
                rookToPosition = new Position('h', move.getFrom().getY());
                rookFromPosition = new Position((char) (move.getTo().getX() - 1), move.getTo().getY());
            } else {
                rookToPosition = new Position('a', move.getFrom().getY());
                rookFromPosition = new Position((char) (move.getTo().getX() + 1), move.getTo().getY());
            }
            // Move the rook back
            int rookFromIndex = bitIndex(rookFromPosition.getX(), rookFromPosition.getY());
            int rookToIndex = bitIndex(rookToPosition.getX(), rookToPosition.getY());
            long rookBitboard = getBitboardForPiece(PieceType.ROOK, move.isColorWhite());
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(PieceType.ROOK, move.isColorWhite(), rookBitboard);
        }
    }

    private void undoPieceMove(Move move, int fromIndex, int toIndex) {
        PieceType movedPieceType = move.getPieceType();
        long pieceBitboard = getBitboardForPiece(movedPieceType, move.isColorWhite());
        pieceBitboard = moveBit(pieceBitboard, toIndex, fromIndex);
        setBitboardForPiece(movedPieceType, move.isColorWhite(), pieceBitboard);
    }

    private void undoPromotion(Move move, int fromIndex, int toIndex) {
        if (move.isPromotionMove()) {
            // Demote the promoted piece back to a pawn
            PieceType promotedTo = move.getPromotionPieceType();
            long promotedPieceBitboard = getBitboardForPiece(promotedTo, move.isColorWhite());
            // Remove promoted piece
            promotedPieceBitboard &= ~(1L << toIndex);
            setBitboardForPiece(promotedTo, move.isColorWhite(), promotedPieceBitboard);

            // Re-add the pawn
            long pawnBitboard = getBitboardForPiece(PieceType.PAWN, move.isColorWhite());
            pawnBitboard |= 1L << fromIndex;
            setBitboardForPiece(PieceType.PAWN, move.isColorWhite(), pawnBitboard);
        }
    }

    private void undoCapture(Move move, int toIndex) {
        if (move.isCapture()) {
            boolean isEnPassantWhite = move.isEnPassantMove() && move.isColorWhite();
            boolean isEnPassantBlack = move.isEnPassantMove() && !move.isColorWhite();

            int enPassantModifier = 0;

            if (isEnPassantWhite) {
                enPassantModifier = -8;
                lastMoveDoubleStepPawnPosition = new Position(move.getTo().getX(), move.getFrom().getY());
            }
            if (isEnPassantBlack) {
                enPassantModifier = 8;
                lastMoveDoubleStepPawnPosition = new Position(move.getTo().getX(), move.getFrom().getY());
            }

            PieceType capturedPieceType = move.getCapturedPieceType();
            long capturedPieceBitboard = getBitboardForPiece(capturedPieceType, !move.isColorWhite());
            // Restore the captured piece on its bitboard
            capturedPieceBitboard |= (1L << toIndex + enPassantModifier);
            setBitboardForPiece(capturedPieceType, !move.isColorWhite(), capturedPieceBitboard);
        }
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
                lastMoveDoubleStepPawnPosition);
    }

    private PieceType getPieceTypeAtSquare(int square) {
        Position position = indexToPosition(square);
        return getPieceTypeAtPosition(position);
    }

    private Color getPieceColorAtSquare(int square) {
        Position position = indexToPosition(square);
        return getPieceColorAtPosition(position);
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

