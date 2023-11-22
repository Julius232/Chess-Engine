package julius.game.chessengine.utils;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.engine.GameStateEnum;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import static julius.game.chessengine.helper.BishopHelper.BISHOP_POSITIONAL_VALUES;
import static julius.game.chessengine.helper.KingHelper.*;
import static julius.game.chessengine.helper.KnightHelper.KNIGHT_POSITIONAL_VALUES;
import static julius.game.chessengine.helper.PawnHelper.*;
import static julius.game.chessengine.helper.QueenHelper.QUEEN_POSITIONAL_VALUES;

@Data
@Log4j2
public class Score {

    public static final int CHECKMATE = 100000;
    public static final int DRAW = 0;

    public static final int KILLER_MOVE_SCORE = Integer.MAX_VALUE;

    private Integer cachedScoreDifference = null;

    private int whiteScore;
    private int blackScore;

    //initialize number of pieces bonus
    private int whitePawns = 0;
    private int blackPawns = 0;
    private int whiteKnights = 0;
    private int blackKnights = 0;
    private int whiteBishops = 0;
    private int blackBishops = 0;
    private int whiteRooks = 0;
    private int blackRooks = 0;
    private int whiteQueens = 0;
    private int blackQueens = 0;

    //agility
    private int agilityWhite = 0;
    private int agilityBlack = 0;

    // Initialize bonuses and penalties for pawns
    private int whiteCenterPawnBonus = 0;
    private int blackCenterPawnBonus = 0;
    private int whiteDoubledPawnPenalty = 0;
    private int blackDoubledPawnPenalty = 0;
    private int whiteIsolatedPawnPenalty = 0;
    private int blackIsolatedPawnPenalty = 0;

    // Initialize positional values
    private int whitePawnsPosition = 0;
    private int blackPawnsPosition = 0;
    private int whiteKnightsPosition = 0;
    private int blackKnightsPosition = 0;
    private int whiteBishopsPosition = 0;
    private int blackBishopsPosition = 0;
    private int whiteQueensPosition = 0;
    private int blackQueensPosition = 0;
    private int whiteKingsPosition = 0;
    private int blackKingsPosition = 0;

    private int whiteStartingSquarePenalty = 0;
    private int blackStartingSquarePenalty = 0;

    // State bonuses
    private int whiteStateBonus = 0;
    private int blackStateBonus = 0;

    // Constants for piece values
    private static final int PAWN_VALUE = 1000;   // Pawns are worth 1 point, scaled by 100
    private static final int KNIGHT_VALUE = 3000; // Knights are worth 3 points
    private static final int BISHOP_VALUE = 3130; // Bishops are worth 3 points
    private static final int ROOK_VALUE = 5000;   // Rooks are worth 5 points
    private static final int QUEEN_VALUE = 9000;  // Queens are worth 9 points

    // Pawn bonuses and penalties
    private static final int CENTER_PAWN_BONUS = 10; // Example bonus value for controlling center squares
    private static final int DOUBLED_PAWN_PENALTY = -20; // Example penalty value for doubled pawns
    private static final int ISOLATED_PAWN_PENALTY = -10; // Penalty for isolated pawns
    private static final int PASSED_PAWN_BONUS = 60;     // Bonus for passed pawns

    // Other bonuses and penalties
    private static final int NOT_CASTLED_AND_ROOK_MOVE_PENALTY = -50;
    private static final int START_POSITION_PENALTY = -50; // Define the penalty value for starting position
    private static final int CASTLING_BONUS = 75;

    // Constants for the initial positions of each piece type
    private static final long INITIAL_WHITE_KNIGHT_POSITION = 0x0000000000000042L; // Knights on b1 and g1
    private static final long INITIAL_BLACK_KNIGHT_POSITION = 0x4200000000000000L; // Knights on b8 and g8
    private static final long INITIAL_WHITE_BISHOP_POSITION = 0x0000000000000024L; // Bishops on c1 and f1
    private static final long INITIAL_BLACK_BISHOP_POSITION = 0x2400000000000000L; // Bishops on c8 and f8
    private static final long INITIAL_WHITE_ROOK_POSITION = 0x0000000000000081L; // Rooks on a1 and h1
    private static final long INITIAL_BLACK_ROOK_POSITION = 0x8100000000000000L; // Rooks on a8 and h8


    public Score() {
        this.whiteScore = 0;
        this.blackScore = 0;
    }

    public Score(Score other) {
        this.whiteScore = other.whiteScore;
        this.blackScore = other.blackScore;

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

        this.agilityWhite = other.agilityWhite;
        this.agilityBlack = other.agilityBlack;

        this.whiteCenterPawnBonus = other.whiteCenterPawnBonus;
        this.blackCenterPawnBonus = other.blackCenterPawnBonus;
        this.whiteDoubledPawnPenalty = other.whiteDoubledPawnPenalty;
        this.blackDoubledPawnPenalty = other.blackDoubledPawnPenalty;
        this.whiteIsolatedPawnPenalty = other.whiteIsolatedPawnPenalty;
        this.blackIsolatedPawnPenalty = other.blackIsolatedPawnPenalty;
        this.whitePawnsPosition = other.whitePawnsPosition;
        this.blackPawnsPosition = other.blackPawnsPosition;
        this.whiteKnightsPosition = other.whiteKnightsPosition;
        this.blackKnightsPosition = other.blackKnightsPosition;
        this.whiteBishopsPosition = other.whiteBishopsPosition;
        this.blackBishopsPosition = other.blackBishopsPosition;
        this.whiteQueensPosition = other.whiteQueensPosition;
        this.blackQueensPosition = other.blackQueensPosition;
        this.whiteKingsPosition = other.whiteKingsPosition;
        this.blackKingsPosition = other.blackKingsPosition;
        this.whiteStartingSquarePenalty = other.whiteStartingSquarePenalty;
        this.blackStartingSquarePenalty = other.blackStartingSquarePenalty;

        this.whiteStateBonus = other.whiteStateBonus;
        this.blackStateBonus = other.blackStateBonus;
    }

    /**
     * Score mechanisms of the Game
     */
    public void initializeScore(BitBoard bitBoard) {
/*        int agilityWhite = bitBoard.generateAllPossibleMoves(true).size();
        int agilityBlack = bitBoard.generateAllPossibleMoves(false).size();*/

        long whitePawns = bitBoard.getWhitePawns();
        long blackPawns = bitBoard.getBlackPawns();
        long whiteKnights = bitBoard.getWhiteKnights();
        long blackKnights = bitBoard.getBlackKnights();
        long whiteBishops = bitBoard.getWhiteBishops();
        long blackBishops = bitBoard.getBlackBishops();
        long whiteRooks = bitBoard.getWhiteRooks();
        long blackRooks = bitBoard.getBlackRooks();
        long whiteQueens = bitBoard.getWhiteQueens();
        long blackQueens = bitBoard.getBlackQueens();
        long whiteKing = bitBoard.getWhiteKing();
        long blackKing = bitBoard.getBlackKing();

        initializePawnScore(whitePawns, blackPawns);
        initializeKnightScore(whiteKnights, blackKnights);
        initializeBishopScore(whiteBishops, blackBishops);
        initializeRookScore(whiteRooks, blackRooks);
        initializeQueenScore(whiteQueens, blackQueens);

        updateCenterPawnBonus(whitePawns, blackPawns);
        updateDoubledPawnPenaltyWhite(whitePawns);
        updateDoubledPawnPenaltyBlack(blackPawns);
        updateIsolatedPawnPenaltyWhite(whitePawns);
        updateIsolatedPawnPenaltyBlack(blackPawns);

        // Apply positional values to the pawns
        updatePawnsPositionBonusWhite(whitePawns);
        updatePawnsPositionBonusBlack(blackPawns);
        updateKnightsPositionBonusWhite(whiteKnights);
        updateKnightsPositionBonusBlack(blackKnights);

        updateBishopsPositionBonusWhite(whiteBishops);
        updateBishopsPositionBonusBlack(blackBishops);
        //                                                          <----- Rook is missing!!!
        updateQueensPositionBonusWhite(whiteQueens);
        updateQueensPositionBonusBlack(blackQueens);

        updateWhiteKingsPositionBonus(whiteKing, bitBoard.isWhiteKingHasCastled(), bitBoard.isWhiteKingMoved(),
                bitBoard.isWhiteRookA1Moved(), bitBoard.isWhiteRookH1Moved(), bitBoard.isEndgame());
        updateBlackKingsPositionBonus(blackKing, bitBoard.isBlackKingHasCastled(), bitBoard.isBlackKingMoved(),
                bitBoard.isBlackRookA8Moved(), bitBoard.isBlackRookH8Moved(), bitBoard.isEndgame());

        updateStartingSquarePenaltyWhite(whiteKnights, whiteBishops, whiteRooks);
        updateStartingSquarePenaltyBlack(blackKnights, blackBishops, blackRooks);

/*        updateAgilityBonus(agilityWhite, agilityBlack);*/
    }


    public int getScoreDifference() {
        if (cachedScoreDifference == null) {
            cachedScoreDifference = calculateTotalWhiteScore() - calculateTotalBlackScore();
        }
        return cachedScoreDifference;
    }

    public int calculateTotalWhiteScore() {
        int totalWhiteScore = 0;

        // Add piece count bonuses
        totalWhiteScore += whitePawns;
        totalWhiteScore += whiteKnights;
        totalWhiteScore += whiteBishops;
        totalWhiteScore += whiteRooks;
        totalWhiteScore += whiteQueens;

        totalWhiteScore += whiteCenterPawnBonus;
        totalWhiteScore += whiteDoubledPawnPenalty;
        totalWhiteScore += whiteIsolatedPawnPenalty;
        totalWhiteScore += whitePawnsPosition;
        totalWhiteScore += whiteKnightsPosition;
        totalWhiteScore += whiteBishopsPosition;
        totalWhiteScore += whiteQueensPosition;
        totalWhiteScore += whiteKingsPosition;
        totalWhiteScore += whiteStartingSquarePenalty;

        totalWhiteScore += whiteStateBonus;

        whiteScore = totalWhiteScore;
        // Add other scores and penalties if any
        return totalWhiteScore;
    }

    /**
     * Method to calculate the total score for black.
     */
    public int calculateTotalBlackScore() {
        int totalBlackScore = 0;

        totalBlackScore += blackPawns;
        totalBlackScore += blackKnights;
        totalBlackScore += blackBishops;
        totalBlackScore += blackRooks;
        totalBlackScore += blackQueens;

        totalBlackScore += blackCenterPawnBonus;
        totalBlackScore += blackDoubledPawnPenalty;
        totalBlackScore += blackIsolatedPawnPenalty;
        totalBlackScore += blackPawnsPosition;
        totalBlackScore += blackKnightsPosition;
        totalBlackScore += blackBishopsPosition;
        totalBlackScore += blackQueensPosition;
        totalBlackScore += blackKingsPosition;
        totalBlackScore += blackStartingSquarePenalty;

        totalBlackScore += blackStateBonus;

        blackScore = totalBlackScore;
        // Add other scores and penalties if any
        return totalBlackScore;
    }

    // Methods to incrementally update scores for specific pieces or actions

    public void initializeQueenScore(long whiteQueens, long blackQueens) {
        this.whiteQueens = Long.bitCount(whiteQueens) * QUEEN_VALUE;
        this.blackQueens = Long.bitCount(blackQueens) * QUEEN_VALUE;
    }

    public void initializeRookScore(long whiteRooks, long blackRooks) {
        this.whiteRooks = Long.bitCount(whiteRooks) * ROOK_VALUE;
        this.blackRooks = Long.bitCount(blackRooks) * ROOK_VALUE;
    }

    public void initializeBishopScore(long whiteBishops, long blackBishops) {
        this.whiteBishops = Long.bitCount(whiteBishops) * BISHOP_VALUE;
        this.blackBishops = Long.bitCount(blackBishops) * BISHOP_VALUE;
    }

    public void initializeKnightScore(long whiteKnights, long blackKnights) {
        this.whiteKnights = Long.bitCount(whiteKnights) * KNIGHT_VALUE;
        this.blackKnights = Long.bitCount(blackKnights) * KNIGHT_VALUE;
    }

    public void initializePawnScore(long whitePawns, long blackPawns) {
        this.whitePawns = Long.bitCount(whitePawns) * PAWN_VALUE;
        this.blackPawns = Long.bitCount(blackPawns) * PAWN_VALUE;
    }

    /**
     * Pawn Bonuses and Penalties
     */
    public void updateCenterPawnBonus(long whitePawns, long blackPawns) {
        whiteCenterPawnBonus = countCenterPawns(whitePawns) * CENTER_PAWN_BONUS;
        blackCenterPawnBonus = countCenterPawns(blackPawns) * CENTER_PAWN_BONUS;
    }

    // Method to add penalty for doubled pawns
    public void updateDoubledPawnPenaltyWhite(long whitePawns) {
        whiteDoubledPawnPenalty = countDoubledPawns(whitePawns) * DOUBLED_PAWN_PENALTY;
    }

    public void updateDoubledPawnPenaltyBlack(long blackPawns) {
        blackDoubledPawnPenalty = countDoubledPawns(blackPawns) * DOUBLED_PAWN_PENALTY;
    }

    // Method to add penalty for isolated pawns
    public void updateIsolatedPawnPenaltyWhite(long whitePawns) {
        whiteIsolatedPawnPenalty = countIsolatedPawns(whitePawns) * ISOLATED_PAWN_PENALTY;
    }

    public void updateIsolatedPawnPenaltyBlack(long blackPawns) {
        blackIsolatedPawnPenalty = countIsolatedPawns(blackPawns) * ISOLATED_PAWN_PENALTY;
    }

    /**
     * Positional Bonuses
     */

    public void updatePawnsPositionBonusWhite(long whitePawns) {
        whitePawnsPosition = applyPositionalValues(whitePawns, WHITE_PAWN_POSITIONAL_VALUES);
    }

    public void updatePawnsPositionBonusBlack(long blackPawns) {
        blackPawnsPosition = applyPositionalValues(blackPawns, BLACK_PAWN_POSITIONAL_VALUES);
    }

    public void updateKnightsPositionBonusWhite(long whiteKnights) {
        whiteKnightsPosition = applyPositionalValues(whiteKnights, KNIGHT_POSITIONAL_VALUES);
    }

    public void updateKnightsPositionBonusBlack(long blackKnights) {
        blackKnightsPosition = applyPositionalValues(blackKnights, KNIGHT_POSITIONAL_VALUES);
    }

    public void updateBishopsPositionBonusWhite(long whiteBishops) {
        whiteBishopsPosition = applyPositionalValues(whiteBishops, BISHOP_POSITIONAL_VALUES);
    }

    public void updateBishopsPositionBonusBlack(long blackBishops) {
        blackBishopsPosition = applyPositionalValues(blackBishops, BISHOP_POSITIONAL_VALUES);
    }

    public void updateQueensPositionBonusWhite(long whiteQueens) {
        whiteQueensPosition = applyPositionalValues(whiteQueens, QUEEN_POSITIONAL_VALUES);
    }

    public void updateQueensPositionBonusBlack(long blackQueens) {
        blackQueensPosition = applyPositionalValues(blackQueens, QUEEN_POSITIONAL_VALUES);
    }

    public void updateWhiteKingsPositionBonus(long whiteKing, boolean isCastled, boolean isWhiteKingMoved, boolean rookA1Moved, boolean rookH1Moved, boolean isEndgame) {
        whiteKingsPosition = applyPositionalValues(whiteKing, isEndgame ? KING_ENDGAME_POSITIONAL_VALUES : WHITE_KING_POSITIONAL_VALUES);
        if (isCastled) {
            whiteKingsPosition += CASTLING_BONUS;
        } else {
            if (rookA1Moved) {
                whiteKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY;
            }
            if (rookH1Moved) {
                whiteKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY;
            }
            if (isWhiteKingMoved) {
                whiteKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY*2;
            }
        }
    }

    public void updateBlackKingsPositionBonus(long blackKing, boolean isCastled, boolean isBlackKingMoved, boolean rookA8Moved, boolean rookH8Moved, boolean isEndgame) {
        blackKingsPosition = applyPositionalValues(blackKing, isEndgame ? KING_ENDGAME_POSITIONAL_VALUES : BLACK_KING_POSITIONAL_VALUES);
        if (isCastled) {
            blackKingsPosition += CASTLING_BONUS;
        } else {
            if (rookA8Moved) {
                blackKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY;
            }
            if (rookH8Moved) {
                blackKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY;
            }
            if (isBlackKingMoved) {
                blackKingsPosition += NOT_CASTLED_AND_ROOK_MOVE_PENALTY*2;
            }
        }
    }

    public void updateStartingSquarePenaltyWhite(long whiteKnights, long whiteBishops, long whiteRooks) {
        // Check if white pieces are all on starting squares
        if (areAllPiecesOnStartingSquares(whiteKnights, whiteBishops, whiteRooks, true)) {
            whiteStartingSquarePenalty = START_POSITION_PENALTY;
        }
        else {
            whiteStartingSquarePenalty = 0;
        }
    }

    public void updateStartingSquarePenaltyBlack(long blackKnights, long blackBishops, long blackRooks) {
        // Check if white pieces are all on starting squares
        if (areAllPiecesOnStartingSquares(blackKnights, blackBishops, blackRooks, false)) {
            blackStartingSquarePenalty = START_POSITION_PENALTY;
        }
        else {
            blackStartingSquarePenalty = 0;
        }
    }

    public void updateAgilityBonus(int movesWhite, int movesBlack) {
        agilityWhite = movesWhite * 10;
        agilityBlack = movesBlack * 10;
    }

    public void updateAgilityBonusWhite(int movesWhite) {
        agilityWhite = movesWhite * 10;
    }

    public void updateAgilityBonusBlack(int movesBlack) {
        agilityBlack = movesBlack * 10;
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

    public void updateWhitePawnValues(long whitePawns) {
        this.whitePawns = Long.bitCount(whitePawns) * PAWN_VALUE;
        updateIsolatedPawnPenaltyWhite(whitePawns);
        updateDoubledPawnPenaltyWhite(whitePawns);
        updatePawnsPositionBonusWhite(whitePawns);
    }

    public void updateBlackPawnValues(long blackPawns) {
        this.blackPawns = Long.bitCount(blackPawns) * PAWN_VALUE;
        updateIsolatedPawnPenaltyBlack(blackPawns);
        updateDoubledPawnPenaltyBlack(blackPawns);
        updatePawnsPositionBonusBlack(blackPawns);
    }

    public void updateWhiteKnightValues(long whiteKnights, long whiteBishops, long whiteRooks) {
        this.whiteKnights = Long.bitCount(whiteKnights) * KNIGHT_VALUE;
        updateKnightsPositionBonusWhite(whiteKnights);
        updateStartingSquarePenaltyWhite(whiteKnights, whiteBishops, whiteRooks);
    }

    public void updateBlackKnightValues(long blackKnights, long blackBishops, long blackRooks) {
        this.blackKnights = Long.bitCount(blackKnights) * KNIGHT_VALUE;
        updateKnightsPositionBonusBlack(blackKnights);
        updateStartingSquarePenaltyBlack(blackKnights, blackBishops, blackRooks);
    }

    public void updateWhiteBishopValues(long whiteBishops, long whiteKnights, long whiteRooks) {
        this.whiteBishops = Long.bitCount(whiteBishops) * BISHOP_VALUE;
        updateBishopsPositionBonusWhite(whiteBishops);
        updateStartingSquarePenaltyWhite(whiteKnights, whiteBishops, whiteRooks);
    }

    public void updateBlackBishopValues(long blackBishops, long blackKnights, long blackRooks) {
        this.blackBishops = Long.bitCount(blackBishops) * BISHOP_VALUE;
        updateBishopsPositionBonusBlack(blackBishops);
        updateStartingSquarePenaltyBlack(blackKnights, blackBishops, blackRooks);
    }

    public void updateWhiteRookValues(long whiteRooks, long whiteKnights, long whiteBishops, long whiteKing, boolean isCastled, boolean isWhiteKingMoved, boolean rookA1Moved, boolean rookH1Moved, boolean isEndgame) {
        this.whiteRooks = Long.bitCount(whiteRooks) * ROOK_VALUE;
        updateStartingSquarePenaltyWhite(whiteKnights, whiteBishops, whiteRooks);
        updateWhiteKingValues(whiteKing, isCastled, isWhiteKingMoved, rookA1Moved, rookH1Moved, isEndgame);
    }

    public void updateBlackRookValues(long blackRooks, long blackKnights, long blackBishops, long blackKing, boolean isCastled, boolean isBlackKingMoved, boolean rookA8Moved, boolean rookH8Moved, boolean isEndgame) {
        this.blackRooks = Long.bitCount(blackRooks) * ROOK_VALUE;
        updateStartingSquarePenaltyBlack(blackKnights, blackBishops, blackRooks);
        updateBlackKingValues(blackKing, isCastled, isBlackKingMoved, rookA8Moved, rookH8Moved, isEndgame);
    }

    public void updateWhiteQueenValues(long whiteQueens) {
        this.whiteQueens = Long.bitCount(whiteQueens) * QUEEN_VALUE;
    }

    public void updateBlackQueenValues(long blackQueens) {
        this.blackQueens = Long.bitCount(blackQueens) * QUEEN_VALUE;
    }

    public void updateWhiteKingValues(long whiteKing, boolean isCastled, boolean isWhiteKingMoved, boolean rookA1Moved, boolean rookH1Moved, boolean isEndgame) {
        updateWhiteKingsPositionBonus(whiteKing, isCastled, isWhiteKingMoved, rookA1Moved, rookH1Moved, isEndgame);
    }

    public void updateBlackKingValues(long blackKing, boolean isCastled, boolean isBlackKingMoved, boolean rookA1Moved, boolean rookH1Moved, boolean isEndgame) {
        updateBlackKingsPositionBonus(blackKing, isCastled, isBlackKingMoved, rookA1Moved, rookH1Moved, isEndgame);
    }

    public void updateStateValuesWhite(GameStateEnum state) {
        if(state.equals(GameStateEnum.BLACK_IN_CHECK)) {
            whiteStateBonus = 250;
        } else if(state.equals(GameStateEnum.WHITE_IN_CHECK)) {
            whiteStateBonus = CHECKMATE;
        }
        else {
            whiteStateBonus = 0;
        }
    }

    public void updateStateValuesBlack(GameStateEnum state) {
        if(state.equals(GameStateEnum.WHITE_IN_CHECK)) {
            blackStateBonus = 250;
        } else if(state.equals(GameStateEnum.BLACK_WON)) {
            blackStateBonus = CHECKMATE;
        }
        else {
            blackStateBonus = 0;
        }
    }

    public void resetCachedStoreDifference() {
        this.cachedScoreDifference = null;
    }
}
