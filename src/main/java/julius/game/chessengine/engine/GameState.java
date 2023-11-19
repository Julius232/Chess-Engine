package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.MoveList;
import julius.game.chessengine.utils.Score;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;

@Data
@Log4j2
public class GameState {

    private ConcurrentHashMap<Long, Integer> repetitionCounter;

    private GameStateEnum state;

    private Score score;

    public GameState(BitBoard bitBoard) {
        repetitionCounter = new ConcurrentHashMap<>();
        state = GameStateEnum.PLAY;
        score = new Score();
        initializeScore(bitBoard);
    }

    public GameState(GameState other) {
        this.repetitionCounter = new ConcurrentHashMap<>(other.repetitionCounter); // Deep copy of the map
        this.state = other.state; // Enum, so a direct copy is fine
        this.score = new Score(other.score);
    }


    private void initializeScore(BitBoard bitBoard) {
        score.initializeScore(bitBoard);
    }

    public void update(BitBoard bitBoard, MoveList legalMoves, int move) {
        updateState(bitBoard, legalMoves);
        updateScore(bitBoard, legalMoves, move);
    }

    public void updateState(BitBoard bitBoard, MoveList legalMoves) {
        if (whiteInCheck(bitBoard)) {
            state = GameStateEnum.WHITE_IN_CHECK;
            if (whiteLost(legalMoves)) {
                state = GameStateEnum.BLACK_WON;
            }
        } else if (blackInCheck(bitBoard)) {
            state = GameStateEnum.BLACK_IN_CHECK;
            if (blackLost(legalMoves)) {
                state = GameStateEnum.WHITE_WON;
            }
        } else if (isDraw(bitBoard, legalMoves)) {
            state = GameStateEnum.DRAW;
        } else {
            state = GameStateEnum.PLAY;
            incrementHashCount(bitBoard.getBoardStateHash());
        }
    }

    public void updateScore(BitBoard bitBoard, MoveList legalMoves, int moveInt) {
        int specialProperty = (moveInt >> 16) & 0x03;
        boolean isWhite = (moveInt & (1 << 15)) != 0;
        int pieceTypeBits = (moveInt >> 12) & 0x07;
        boolean isCapture = (specialProperty & 0x01) != 0;
        int promotionPieceTypeBits = (moveInt >> 18) & 0x07;
        int capturedPieceTypeBits = (moveInt >> 21) & 0x07;
        boolean isEnPassantMove = specialProperty == 3;
        boolean isCastlingMove = specialProperty == 2;
        boolean isKingFirstMove = (moveInt & (1 << 24)) != 0;
        boolean isRookFirstMove = (moveInt & (1 << 25)) != 0;

        updatePieceValues(isWhite, pieceTypeBits, bitBoard, legalMoves);
        if (capturedPieceTypeBits != 0) {
            updateCapturedPieceValues(isWhite, capturedPieceTypeBits, bitBoard);
        }

        log.debug("Piecetype: {}, CapturedType: {}, ScoreWhite: {}, ScoreBlack: {}",
                pieceTypeBits, capturedPieceTypeBits, score.calculateTotalWhiteScore(), score.calculateTotalBlackScore());
    }

    private void updatePieceValues(boolean isWhite, int pieceTypeBits, BitBoard bitBoard, MoveList legalMoves) {
        if (isWhite) {
            updateValuesForWhite(pieceTypeBits, bitBoard);
            score.updateAgilityBonusWhite(legalMoves.size());

        } else {
            updateValuesForBlack(pieceTypeBits, bitBoard);
            score.updateAgilityBonusBlack(legalMoves.size());
        }


    }

    private void updateValuesForWhite(int pieceTypeBits, BitBoard bitBoard) {
        boolean isEndgame = bitBoard.getWhiteQueens() == 0 && bitBoard.getBlackQueens() == 0;
        switch (pieceTypeBits) {
            case 1: score.updateWhitePawnValues(bitBoard.getWhitePawns()); break;
            case 2: score.updateWhiteKnightValues(bitBoard.getWhiteKnights(), bitBoard.getWhiteBishops(), bitBoard.getWhiteRooks()); break;
            case 3: score.updateWhiteBishopValues(bitBoard.getWhiteBishops(), bitBoard.getWhiteKnights(), bitBoard.getWhiteRooks()); break;
            case 4: score.updateWhiteRookValues(bitBoard.getWhiteRooks(), bitBoard.getWhiteKnights(), bitBoard.getWhiteBishops(), bitBoard.getWhiteKing(), bitBoard.isWhiteKingHasCastled(), bitBoard.isWhiteRookA1Moved(), bitBoard.isWhiteRookH1Moved(), isEndgame); break;
            case 5: score.updateWhiteQueenValues(bitBoard.getWhiteQueens()); break;
            case 6: score.updateWhiteKingValues(bitBoard.getWhiteKing(), bitBoard.isWhiteKingHasCastled(), bitBoard.isWhiteRookA1Moved(), bitBoard.isWhiteRookH1Moved(), bitBoard.getWhiteQueens() == 0 && bitBoard.getBlackQueens() == 0); break;
            default: break; // Optionally handle default case
        }
    }

    private void updateValuesForBlack(int pieceTypeBits, BitBoard bitBoard) {
        boolean isEndgame = bitBoard.getWhiteQueens() == 0 && bitBoard.getBlackQueens() == 0;
        switch (pieceTypeBits) {
            case 1: score.updateBlackPawnValues(bitBoard.getBlackPawns()); break;
            case 2: score.updateBlackKnightValues(bitBoard.getBlackKnights(), bitBoard.getBlackBishops(), bitBoard.getBlackRooks()); break;
            case 3: score.updateBlackBishopValues(bitBoard.getBlackBishops(), bitBoard.getBlackKnights(), bitBoard.getBlackRooks()); break;
            case 4: score.updateBlackRookValues(bitBoard.getBlackRooks(), bitBoard.getBlackKnights(), bitBoard.getBlackBishops(), bitBoard.getBlackKing(), bitBoard.isBlackKingHasCastled(), bitBoard.isBlackRookA8Moved(), bitBoard.isBlackRookH8Moved(), isEndgame); break;
            case 5: score.updateBlackQueenValues(bitBoard.getBlackQueens()); break;
            case 6: score.updateBlackKingValues(bitBoard.getBlackKing(), bitBoard.isBlackKingHasCastled(), bitBoard.isBlackRookA8Moved(), bitBoard.isBlackRookH8Moved(), isEndgame); break;
            default: break; // Optionally handle default case
        }
    }

    private void updateCapturedPieceValues(boolean isWhite, int capturedPieceTypeBits, BitBoard bitBoard) {
        if (isWhite) {
            updateValuesForBlack(capturedPieceTypeBits, bitBoard); // Update black pieces if white is capturing
        } else {
            updateValuesForWhite(capturedPieceTypeBits, bitBoard); // Update white pieces if black is capturing
        }
        //Endgamecheck if the queens are gone
        if(capturedPieceTypeBits == 5 && bitBoard.getBlackQueens() == 0 && bitBoard.getWhiteQueens() == 0) {
            score.updateWhiteKingValues(bitBoard.getWhiteKing(), bitBoard.isWhiteKingHasCastled(), bitBoard.isWhiteRookA1Moved(), bitBoard.isWhiteRookH1Moved(), true);
            score.updateBlackKingValues(bitBoard.getBlackKing(), bitBoard.isBlackKingHasCastled(), bitBoard.isBlackRookA8Moved(), bitBoard.isBlackRookH8Moved(), true);
        }
    }


    /**
     * State mechanisms of the Game
     */

    public boolean isGameOver() {
        return isInStateCheckMate() || isInStateDraw();
    }

    public boolean isInStateCheck() {
        // The BitBoard class already has a method to check if a king is in check
        return state.equals(GameStateEnum.BLACK_IN_CHECK) || state.equals(GameStateEnum.WHITE_IN_CHECK);
    }

    public boolean isInStateCheckMate() {
        return state.equals(GameStateEnum.WHITE_WON) || state.equals(GameStateEnum.BLACK_WON);
    }

    public boolean isInStateDraw() {
        return state.equals(GameStateEnum.DRAW);
    }

    private boolean whiteInCheck(BitBoard bitBoard) {
        return bitBoard.isInCheck(true);
    }

    private boolean blackInCheck(BitBoard bitBoard) {
        return bitBoard.isInCheck(false);
    }

    private boolean whiteLost(MoveList legalMoves) {
        return state.equals(GameStateEnum.WHITE_IN_CHECK) && legalMoves.size() == 0;
    }

    private boolean blackLost(MoveList legalMoves) {
        return state.equals(GameStateEnum.BLACK_IN_CHECK) && legalMoves.size() == 0;
    }


    private boolean isDraw(BitBoard bitBoard, MoveList legalMoves) {
        boolean insufficientMaterial = bitBoard.hasInsufficientMaterial();
        boolean isThreeFoldRepetition = isThreeFoldRepetition(bitBoard.getBoardStateHash());

        if (insufficientMaterial) {
            log.info("Insufficient Material");
        }
        if (isThreeFoldRepetition) {
            log.info("ThreeFoldRepetition");
        }
        return legalMoves.size() == 0 || insufficientMaterial || isThreeFoldRepetition;
    }

    /**
     * Threefold Repetition Logic
     */
    private void incrementHashCount(long hash) {
        repetitionCounter.put(hash, repetitionCounter.getOrDefault(hash, 0) + 1);
    }

    private void decrementHashCount(long hash) {
        // Check if the hash exists in the map
        if (repetitionCounter.containsKey(hash)) {
            int count = repetitionCounter.get(hash);

            // Decrement the count
            if (count > 1) {
                repetitionCounter.put(hash, count - 1);
            } else {
                // If the count reaches zero, remove the hash from the map
                repetitionCounter.remove(hash);
            }
        }
    }

    private boolean isThreeFoldRepetition(long hash) {
        int repCount = repetitionCounter.getOrDefault(hash, 0);
        if (repCount > 3) {
            throw new IllegalStateException(String.format("Repetition count can't be higher then 3, was %s", repCount));
        }
        return repCount == 3;
    }

    public void undo(long hash) {
        decrementHashCount(hash);
        state = GameStateEnum.PLAY;
    }

    @Override
    public String toString() {
        String sb = "GameState {" +
                "\n  State: " + state +
                "\n  White Score: " + score.calculateTotalWhiteScore() +
                "\n  Black Score: " + score.calculateTotalBlackScore() +
                "\n  Score Difference: " + score.getScoreDifference() +
                "\n  Repetition Count: " + repetitionCounter +
                "\n}";
        return sb;
    }
}