package julius.game.chessengine.engine;

import julius.game.chessengine.board.BitBoard;
import julius.game.chessengine.board.MoveList;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Data
@Log4j2
public class GameState {

    private Map<Long, Integer> repetitionCounter;

    private GameStateEnum state;

    public GameState() {
        repetitionCounter = new HashMap<>();
        state = GameStateEnum.PLAY;
    }

    public GameState(GameState other) {
        this.repetitionCounter = new HashMap<>(other.repetitionCounter); // Deep copy of the map
        this.state = other.state; // Enum, so a direct copy is fine
    }


    public void update(BitBoard bitBoard, MoveList legalMoves) {
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
}