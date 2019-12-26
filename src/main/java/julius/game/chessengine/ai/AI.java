package julius.game.chessengine.ai;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Log4j2
@Component
public class AI {

    private final Engine engine;

    public AI(Engine engine) {
        this.engine = engine;
    }

    public GameState executeCalculatedMove(String color) {
        MoveField calculatedMove = calculateMove(engine.getBoard(), color);
        return engine.moveFigure(engine.getBoard(), calculatedMove.fromPositionToString(), calculatedMove.toPositionToString());
    }

    private MoveField calculateMove(Board board, String color) {
        List<MoveField> moveFields = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);

        MoveField calculatedMoveField = moveFields.stream()
                .max(Comparator.comparing(moveField -> engine.simulateMoveAndGetEfficiency(board, moveField, color, 2)))
                .orElseThrow(() -> new RuntimeException("No possible movefields for AI"));

        log.info("Calculated Move is From: " + calculatedMoveField.fromPositionToString()
                + " To: " + calculatedMoveField.toPositionToString());


        return calculatedMoveField;
    }




}
