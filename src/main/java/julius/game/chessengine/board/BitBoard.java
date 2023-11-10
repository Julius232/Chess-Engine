package julius.game.chessengine.board;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.PieceType;
import julius.game.chessengine.helper.PawnHelper;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static julius.game.chessengine.helper.BitHelper.*;
import static julius.game.chessengine.helper.KnightHelper.knightMoves;
import static julius.game.chessengine.helper.PawnHelper.*;

@Log4j2
public class BitBoard {
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
    private Position lastMoveDoubleStepPawnPosition;

    // Flags to track if the king and rooks have moved
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookA1Moved = false;
    private boolean whiteRookH1Moved = false;
    private boolean blackRookA8Moved = false;
    private boolean blackRookH8Moved = false;

    public List<Move> getAllCurrentPossibleMoves() {
        return generateAllPossibleMoves(whitesTurn ? Color.WHITE : Color.BLACK);
    }

    public BitBoard() {
        this.currentScore = new Score(0, 0);
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
        // Define additional score values
        final int CENTER_PAWN_BONUS = 10;   // Bonus points for pawns in the center
        final int DOUBLED_PAWN_PENALTY = -20; // Penalty points for doubled pawns
        final int ISOLATED_PAWN_PENALTY = -10; // Penalty points for isolated pawns

        // Initialize bonuses and penalties
        int whiteCenterBonus = 0;
        int blackCenterBonus = 0;
        int whiteDoubledPenalty = 0;
        int blackDoubledPenalty = 0;
        int whiteIsolatedPenalty = 0;
        int blackIsolatedPenalty = 0;

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

        // Return the score encapsulated in a Score object
        this.currentScore = new Score(whiteScore, blackScore);
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

    private boolean isPieceUnderThreat(long pieceBitboard, Color color) {
        // Convert the piece bitboard into positions and check for threats against each position
        while (pieceBitboard != 0) {
            int positionIndex = Long.numberOfTrailingZeros(pieceBitboard);
            Position piecePosition = indexToPosition(positionIndex);

            // If any opponent's piece can legally move to the piece position, then the piece is under threat
            if (isPositionUnderThreat(piecePosition, color)) {
                return true;
            }

            // Clear the checked position bit so that we can proceed to the next piece
            pieceBitboard &= pieceBitboard - 1;
        }
        return false;
    }

    private boolean isPositionUnderThreat(Position position, Color color) {
        // Get the opponent's color
        Color opponentColor = getOpponentColor(color);

        // Check if any of the opponent's piece attacks include the position
        // For example, you would check if any opponent's pawns, knights, bishops, rooks, queens, or king can attack the position
        return canPawnAttackPosition(position, opponentColor) ||
                canKnightAttackPosition(position, opponentColor) ||
                canBishopAttackPosition(position, opponentColor) ||
                canRookAttackPosition(position, opponentColor) ||
                canQueenAttackPosition(position, opponentColor) ||
                canKingAttackPosition(position, opponentColor);
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

    void updateAggregatedBitboards() {
        whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
        blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
        allPieces = whitePieces | blackPieces;
        updateScore();
    }

    private List<Move> generatePawnMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
        long opponentPieces = (color == Color.WHITE) ? blackPieces : whitePieces;
        long emptySquares = ~(whitePieces | blackPieces);

        log.debug("Initial pawns: {}", "0x" + Long.toHexString(pawns));
        log.debug("Opponent pieces: {}", "0x" + Long.toHexString(opponentPieces));
        log.debug("Empty squares: {}", "0x" + Long.toHexString(emptySquares));

        // Moves the pawns one rank up
        long singleStepForward = (color == Color.WHITE) ? pawns << 8 : pawns >>> 8;
        singleStepForward &= emptySquares;
        log.debug("Single step forward: {}", "0x" + Long.toHexString(singleStepForward));

        // Finds pawns on their initial rank that can move two steps forward

        long doubleStepForward = 0L; // Initialize the variable

        // For white pawns on their starting rank, move two steps forward if both squares are empty.
        if (color == Color.WHITE) {
            long whitePawnsOnStartingRank = pawns & RankMasks[1];
            long whitePawnsSingleStep = whitePawnsOnStartingRank << 8;
            long whitePawnsDoubleStep = whitePawnsSingleStep << 8;
            doubleStepForward = whitePawnsDoubleStep & emptySquares;
        }

        // For black pawns on their starting rank, move two steps forward if both squares are empty.
        if (color == Color.BLACK) {
            long blackPawnsOnStartingRank = pawns & RankMasks[6];
            long blackPawnsSingleStep = blackPawnsOnStartingRank >>> 8;
            long blackPawnsDoubleStep = blackPawnsSingleStep >>> 8;
            doubleStepForward = blackPawnsDoubleStep & emptySquares;
        }
        log.debug("Double step forward: {}", "0x" + Long.toHexString(doubleStepForward));

        // Calculate potential capture moves to the left and right
        long attacksLeft = (color == Color.WHITE) ? (pawns & ~FileMasks[0]) << 7 : (pawns & ~FileMasks[0]) >>> 9;
        long attacksRight = (color == Color.WHITE) ? (pawns & ~FileMasks[7]) << 9 : (pawns & ~FileMasks[7]) >>> 7;

        log.debug("Attacks left before masking: {}", "0x" + Long.toHexString(attacksLeft));
        log.debug("Attacks right before masking: {}", "0x" + Long.toHexString(attacksRight));

        // Filter out captures that don't have an opponent piece
        attacksLeft &= opponentPieces;
        attacksRight &= opponentPieces;

        log.debug("Attacks left after masking: {}", "0x" + Long.toHexString(attacksLeft));
        log.debug("Attacks right after masking: {}", "0x" + Long.toHexString(attacksRight));

        addPawnMoves(moves, singleStepForward, 8, false, color);
        addPawnMoves(moves, doubleStepForward, 16, false, color);
        addPawnMoves(moves, attacksLeft, (color == Color.WHITE) ? 7 : 9, true, color);
        addPawnMoves(moves, attacksRight, (color == Color.WHITE) ? 9 : 7, true, color);


        if (lastMoveDoubleStepPawnPosition != null) {
            int enPassantRank = (color == Color.WHITE) ? 5 : 2; // For white, the en passant rank is 6 (5 in 0-based index); for black, it's 3 (2 in 0-based index).
            int fileIndexOfDoubleSteppedPawn = lastMoveDoubleStepPawnPosition.getX() - 'a'; // Convert file character to 0-based index.

            // The en passant target square is the square passed over by the double-stepping pawn
            int enPassantTargetIndex = (enPassantRank * 8) + fileIndexOfDoubleSteppedPawn;
            long enPassantTargetSquare = 1L << enPassantTargetIndex;

            // Pawns that can potentially perform an en passant capture must be on the rank adjacent to the en passant target square
            long potentialEnPassantAttackers = pawns & RankMasks[color == Color.WHITE ? 4 : 3];

            // Make sure to add boundary checks here to prevent ArrayIndexOutOfBoundsException
            if (fileIndexOfDoubleSteppedPawn > 0) { // There is a file to the left
                long leftAttackers = potentialEnPassantAttackers & FileMasks[fileIndexOfDoubleSteppedPawn - 1];
                if (color.equals(Color.WHITE) ?
                        ((leftAttackers << 9 & enPassantTargetSquare) != 0) :
                        ((leftAttackers >> 7 & enPassantTargetSquare) != 0)) {
                    int fromIndex = Long.numberOfTrailingZeros(leftAttackers);
                    addEnPassantMove(moves, fromIndex, enPassantTargetIndex, color);
                }
            }

            if (fileIndexOfDoubleSteppedPawn < 7) { // There is a file to the right
                long rightAttackers = potentialEnPassantAttackers & FileMasks[fileIndexOfDoubleSteppedPawn + 1];
                if (color.equals(Color.WHITE) ?
                        ((rightAttackers << 7 & enPassantTargetSquare) != 0) :
                        ((rightAttackers >> 9 & enPassantTargetSquare) != 0)) {
                    int fromIndex = Long.numberOfTrailingZeros(rightAttackers);
                    addEnPassantMove(moves, fromIndex, enPassantTargetIndex, color);
                }
            }
        }


        return moves;
    }

    private void addEnPassantMove(List<Move> moves, int fromIndex, int toIndex, Color color) {
        Position fromPosition = indexToPosition(fromIndex);
        Position toPosition = indexToPosition(toIndex);
        moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, true, false, true, null, PieceType.PAWN, false, false));
    }

    private void addPawnMoves(List<Move> moves, long bitboard, int shift, boolean isCapture, Color color) {
        while (bitboard != 0) {
            int toIndex = Long.numberOfTrailingZeros(bitboard);
            int fromIndex = color == Color.WHITE ? toIndex - shift : toIndex + shift;
            Position fromPosition = indexToPosition(fromIndex);
            Position toPosition = indexToPosition(toIndex);
            boolean isPromotion = (color == Color.WHITE) ? toPosition.getY() == 8 : toPosition.getY() == 1;

            PieceType capturedType = isCapture ? getPieceTypeAtPosition(toPosition) : null;

            if (isPromotion) {
                moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, isCapture, false, false, PieceType.QUEEN, capturedType, false, false));
                moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, isCapture, false, false, PieceType.ROOK, capturedType, false, false));
                moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, isCapture, false, false, PieceType.BISHOP, capturedType, false, false));
                moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, isCapture, false, false, PieceType.KNIGHT, capturedType, false, false));
            } else {
                moves.add(new Move(fromPosition, toPosition, PieceType.PAWN, color, isCapture, false, false, null, capturedType, false, false));
            }

            bitboard &= bitboard - 1; // Clear the processed bit
        }
    }


    private List<Move> generateKnightMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        long knights = (color == Color.WHITE) ? whiteKnights : blackKnights;
        long ownPieces = (color == Color.WHITE) ? whitePieces : blackPieces;



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
                            moves.add(new Move(fromPosition, toPosition, PieceType.KNIGHT, color, isCapture, false, false, null, capturedPieceType, false, false));
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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                            moves.add(new Move(fromPosition, toPosition, PieceType.BISHOP, color, isCapture, false, false, null, capturedPieceType, false, false));

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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;

                            boolean isFirstRookMove = !hasRookMoved(fromPosition);

                            moves.add(new Move(fromPosition, toPosition, PieceType.ROOK, color, isCapture, false, false, null, capturedPieceType, false, isFirstRookMove));

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
                            PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                            moves.add(new Move(fromPosition, toPosition, PieceType.QUEEN, color, isCapture, false, false, null, capturedPieceType, false, false));

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

        boolean isFirstKingMove = !hasKingMoved(color);

        for (int offset : offsets) {
            int targetIndex = kingPositionIndex + offset;
            // Check if the move is within board bounds and does not wrap around
            if (targetIndex >= 0 && targetIndex < 64 && !doesMoveWrapAround(kingPositionIndex, targetIndex)) {
                Position toPosition = indexToPosition(targetIndex);
                if (!isOccupiedByColor(toPosition, color)) {
                    boolean isCapture = isOccupiedByOpponent(toPosition, color);
                    PieceType capturedPieceType = isCapture ? getPieceTypeAtPosition(toPosition) : null;
                    moves.add(new Move(kingPosition, toPosition, PieceType.KING, color, isCapture, false, false, null, capturedPieceType, isFirstKingMove, false));
                }
            }
        }

        // Castling logic
        // The actual castling checks should be implemented in the canCastleKingside and canCastleQueenside methods
        if (canKingCastle(color)) {
            // Kingside castling
            if (canCastleKingside(color, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex + 2), PieceType.KING, color, false, true, false, null, null, true, true));
            }
            // Queenside castling
            if (canCastleQueenside(color, kingPositionIndex)) {
                moves.add(new Move(kingPosition, indexToPosition(kingPositionIndex - 2), PieceType.KING, color, false, true, false, null, null, true, true));
            }
        }

        return moves;
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

        if (move.isPromotionMove()) {
            // Clear the pawn from the promotion square
            clearSquare(toIndex, move.getColor());

            // Set the bitboard for the promotion piece
            long promotionPieceBitboard = getBitboardForPiece(move.getPromotionPieceType(), move.getColor());
            promotionPieceBitboard |= (1L << toIndex); // Place the promotion piece on the promotion square
            setBitboardForPiece(move.getPromotionPieceType(), move.getColor(), promotionPieceBitboard);
        }

        // Mark the king as moved if it was a king move
        if (move.getPieceType() == PieceType.KING) {
            markKingAsMoved(move.getColor());
        }

        if (move.getPieceType() == PieceType.ROOK) {
            markRookAsMoved(indexToPosition(fromIndex));
        }

        if (move.getPieceType() == PieceType.PAWN && Math.abs(move.getFrom().getY() - move.getTo().getY()) == 2) {
            lastMoveDoubleStepPawnPosition = move.getTo();
        } else {
            lastMoveDoubleStepPawnPosition = null;
        }

        updateAggregatedBitboards();
        whitesTurn = !whitesTurn;
    }


    public void clearSquare(int index, Color color) {
        long mask = ~(1L << index);
        if (color == Color.WHITE) {
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
        return fileDifference == 0 &&
                to.getY() == (lastMoveDoubleStepPawnPosition.getY() == 5 ? lastMoveDoubleStepPawnPosition.getY() + 1 : lastMoveDoubleStepPawnPosition.getY() - 1);
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

    private boolean isValidBoardPosition(Position position) {
        return position.getX() >= 'a' && position.getX() <= 'h' && position.getY() >= 1 && position.getY() <= 8;
    }

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
        return canKingAttackKing(kingPosition, color);

        // If none of the pieces can attack the king, then the king is not in check
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
        int pawnAttackDirection = (kingColor == Color.WHITE) ? 1 : -1;
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

    public void logBoardWithDepth(int depth) {
        log.info("  -------------------  DEPTH[{}]  -------------------  ", depth);
        logBoard();
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

    public void undoMove(Move move) {


        int fromIndex = bitIndex(move.getFrom().getX(), move.getFrom().getY());
        int toIndex = bitIndex(move.getTo().getX(), move.getTo().getY());

        // 1. Handle Captured Piece Restoration
        if (move.isCapture()) {
            boolean isEnPassantWhite = move.isEnPassantMove() && move.getColor() == Color.WHITE;
            boolean isEnPassantBlack = move.isEnPassantMove() && move.getColor() == Color.BLACK;

            int enPassantModifier = 0;

            if(isEnPassantWhite) {
                enPassantModifier = -8;
                lastMoveDoubleStepPawnPosition = new Position(move.getTo().getX(), move.getFrom().getY());
            }
            if (isEnPassantBlack) {
                enPassantModifier = +8;
                lastMoveDoubleStepPawnPosition = new Position(move.getTo().getX(), move.getFrom().getY());
            }

            PieceType capturedPieceType = move.getCapturedPieceType();
            Color opponentColor = getOpponentColor(move.getColor());
            long capturedPieceBitboard = getBitboardForPiece(capturedPieceType, opponentColor);
            // Restore the captured piece on its bitboard
            capturedPieceBitboard |= (1L << toIndex + enPassantModifier);
            setBitboardForPiece(capturedPieceType, opponentColor, capturedPieceBitboard);
        }


        // 2. Handle Pawn Promotion
        if (move.isPromotionMove()) {
            // Demote the promoted piece back to a pawn
            PieceType promotedTo = move.getPromotionPieceType();
            long promotedPieceBitboard = getBitboardForPiece(promotedTo, move.getColor());
            // Remove promoted piece
            promotedPieceBitboard &= ~(1L << toIndex);
            setBitboardForPiece(promotedTo, move.getColor(), promotedPieceBitboard);

            // Re-add the pawn
            long pawnBitboard = getBitboardForPiece(PieceType.PAWN, move.getColor());
            pawnBitboard |= 1L << fromIndex;
            setBitboardForPiece(PieceType.PAWN, move.getColor(), pawnBitboard);
        }

        // Moving the piece back...
        // Ensure that if the piece is a king, it's handled correctly
        PieceType movedPieceType = move.getPieceType();
        long pieceBitboard = getBitboardForPiece(movedPieceType, move.getColor());
        pieceBitboard = moveBit(pieceBitboard, toIndex, fromIndex);
        setBitboardForPiece(movedPieceType, move.getColor(), pieceBitboard);

        // If the move was a castling move, move the rook back
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
            long rookBitboard = getBitboardForPiece(PieceType.ROOK, move.getColor());
            rookBitboard = moveBit(rookBitboard, rookFromIndex, rookToIndex);
            setBitboardForPiece(PieceType.ROOK, move.getColor(), rookBitboard);
        }

        // If the move was a double pawn push, remove the last move double step pawn position
        if (move.getPieceType() == PieceType.PAWN && Math.abs(move.getFrom().getY() - move.getTo().getY()) == 2) {
            lastMoveDoubleStepPawnPosition = null;
        }

        // Restore the state of the king and rook moved flags if necessary
        if (move.getPieceType() == PieceType.KING && move.isKingFirstMove()) {
            if (move.getColor() == Color.WHITE) {
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

        // Update the aggregated bitboards
        updateAggregatedBitboards();

        whitesTurn = !whitesTurn;
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
                blackRookH8Moved == bitBoard.blackRookH8Moved &&
                (Objects.equals(lastMoveDoubleStepPawnPosition, bitBoard.lastMoveDoubleStepPawnPosition));
    }

    @Override
    public int hashCode() {
        return Objects.hash(whitePawns, blackPawns, whiteKnights, blackKnights, whiteBishops, blackBishops,
                whiteRooks, blackRooks, whiteQueens, blackQueens, whiteKing, blackKing,
                whitePieces, blackPieces, allPieces, whiteKingMoved, blackKingMoved,
                whiteRookA1Moved, whiteRookH1Moved, blackRookA8Moved, blackRookH8Moved,
                lastMoveDoubleStepPawnPosition);
    }

    public long getBoardStateHash() {
        long hash = 0;


        // Combine the hash codes of all individual piece bitboards and other state indicators
        hash ^= whitePawns ^ blackPawns ^ whiteKnights ^ blackKnights ^ whiteBishops ^ blackBishops;
        hash ^= whiteRooks ^ blackRooks ^ whiteQueens ^ blackQueens ^ whiteKing ^ blackKing;
        hash ^= (whiteKingMoved ? 1 : 0) ^ (blackKingMoved ? 2 : 0);
        hash ^= (whiteRookA1Moved ? 4 : 0) ^ (whiteRookH1Moved ? 8 : 0);
        hash ^= (blackRookA8Moved ? 16 : 0) ^ (blackRookH8Moved ? 32 : 0);
        hash ^= (whitesTurn ? 64 : 0); // Assuming you have a field indicating whose turn it is
        return hash;
    }
}

