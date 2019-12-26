package julius.game.chessengine.ai;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

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
        String fenBoard = FEN.translateBoardToFEN(board).getRenderBoard();
        int levelOfDepth = 1;
        String filePath = "src/main/resources/best.moves.level" + levelOfDepth;

        List<MoveField> moveFields = engine.getAllPossibleMoveFieldsForPlayerColor(board, color);

        try {
            Map<String, MoveField> bestMoves = new HashMap<>();
            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));
            for (String key : properties.stringPropertyNames()) {
                bestMoves.put(key, new MoveField(board, properties.get(key).toString()));
            }
            if(bestMoves.containsKey(fenBoard)) {
                MoveField bestMove = bestMoves.get(fenBoard);
                log.info("Found bestMove for " + fenBoard + " = " + bestMove.toString() + " in " + filePath);
                return bestMove;
            }

            Map<MoveField, Double> calculatedMoveMap = moveFields.parallelStream()
                    .collect(Collectors.toMap(moveField -> moveField, moveField ->
                    {
                        log.info("Calculating:" + moveField.toString() + " + levelOfDepth = " + levelOfDepth);
                        double efficiency = engine.simulateMoveAndGetEfficiency(board, moveField, color, color, levelOfDepth, -3333);
                        log.info("Efficiency for Move: " + moveField.toString() + " was " + efficiency);
                        return efficiency;
                    }));

            MoveField calculatedMoveField = calculatedMoveMap.entrySet().parallelStream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new RuntimeException("No possible movefields for AI"));

            log.info("Calculated Move is From: " + calculatedMoveField.fromPositionToString()
                    + " To: " + calculatedMoveField.toPositionToString());
            bestMoves.put(FEN.translateBoardToFEN(board).getRenderBoard(), calculatedMoveField);

            for (Map.Entry<String,MoveField> entry : bestMoves.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().toString());
            }

            properties.store(new FileOutputStream(filePath), null);

            return calculatedMoveField;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;

    }




}
