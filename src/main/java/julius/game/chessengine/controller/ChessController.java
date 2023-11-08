package julius.game.chessengine.controller;

import julius.game.chessengine.ai.AI;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Controller
@RequestMapping(value = "/chess")
@RequiredArgsConstructor
public class ChessController {

    private final AI ai;
    private final Engine engine;

    @GetMapping(value = "/score")
    public ResponseEntity<Score> getScore() {
        return ResponseEntity.ok(engine.getBitBoard().getScore());
    }

    @PutMapping(value = "/reset")
    public ResponseEntity<?> resetBoard() {
        engine.startNewGame();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/field/possible/white")
    public ResponseEntity<List<Move>> getAllPossibleFieldsWhite() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsForPlayerColor(Color.WHITE));
    }

    @GetMapping(value = "/field/possible/black")
    public ResponseEntity<List<Move>> getAllPossibleFieldsBlack() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsForPlayerColor(Color.BLACK));
    }

    @GetMapping(value = "/figure")
    public ResponseEntity<List<Figure>> getFigures() {
        return ResponseEntity.ok(engine.getBitBoard().getFigures());
    }

    @GetMapping(value = "/figure/frontend")
    public ResponseEntity<FEN> getFiguresFrontend() {
        return ResponseEntity.ok(FEN.translateBoardToFEN(engine.getBitBoard()));
    }

    @PatchMapping(value = "/figure/move/{from}/{to}")
    public ResponseEntity<GameState> moveFigure(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        if (from != null && to != null) {
            GameState state = engine.moveFigure(engine.getBitBoard(), convertStringToPosition(from), convertStringToPosition(to));
            return ResponseEntity.ok(state);
        } else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value = "/figure/move/intelligent/{color}")
    public ResponseEntity<GameState> calculateMoveForColor(@PathVariable("color") String color) {
        if (color != null) {
            return ResponseEntity.ok(ai.executeCalculatedMove(color));
        } else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value = "/figure/move/random/{color}")
    public ResponseEntity<GameState> moveRandomFigure(@PathVariable("color") String color) {
        if (color != null) {
            return ResponseEntity.ok(engine.moveRandomFigure(Color.fromString(color)));
        } else return ResponseEntity.status(406).build();
    }

    @GetMapping(value = "/figure/move/possible/{from}")
    public ResponseEntity<List<Position>> getPossibleToPositions(@PathVariable("from") String from) {
        if (from != null) {
            return ResponseEntity.ok(engine.getPossibleMovesForPosition(convertStringToPosition(from)));
        } else return ResponseEntity
                .status(406)
                .build();
    }

    public Position convertStringToPosition(String positionStr) {
        if (positionStr.length() != 2) {
            throw new IllegalArgumentException("Invalid position string: " + positionStr);
        }
        char file = positionStr.charAt(0);
        int rank = Character.getNumericValue(positionStr.charAt(1));

        if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Position out of bounds: " + positionStr);
        }

        return new Position(file, rank);
    }
}
