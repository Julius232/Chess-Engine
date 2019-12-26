package julius.game.chessengine.controller;

import julius.game.chessengine.ai.AI;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.MoveField;
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
    public ResponseEntity<Score> getScore(){
        return ResponseEntity.ok(engine.getBoard().getScore());
    }

    @PutMapping(value = "/reset")
    public ResponseEntity<?> resetBoard() {
        engine.startNewGame();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/field")
    public ResponseEntity<List<Field>> getFields() {
        return ResponseEntity.ok(engine.getBoard().getFields());
    }

    @GetMapping(value = "/field/possible/white")
    public ResponseEntity<List<MoveField>> getAllPossibleFieldsWhite() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsForPlayerColor(engine.getBoard(), Color.WHITE));
    }

    @GetMapping(value = "/field/possible/black")
    public ResponseEntity<List<MoveField>> getAllPossibleFieldsBlack() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsForPlayerColor(engine.getBoard(), Color.BLACK));
    }

    @GetMapping(value = "/figure")
    public ResponseEntity<List<Figure>> getFigures() {
        return ResponseEntity.ok(engine.getBoard().getFigures());
    }

    @GetMapping(value = "/figure/frontend")
    public ResponseEntity<FEN> getFiguresFrontend() {return ResponseEntity.ok(FEN.translateBoardToFEN(engine.getBoard()));}

    @PatchMapping(value="/figure/move/{from}/{to}")
    public ResponseEntity<GameState> moveFigure(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        if(from != null && to != null) {
            return ResponseEntity.ok(engine.moveFigure(engine.getBoard(), from, to));
        }
        else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value="/figure/move/intelligent/{color}")
    public ResponseEntity<GameState> calculateMoveForColor(@PathVariable("color") String color) {
        if(color != null) {
            return ResponseEntity.ok(ai.executeCalculatedMove(color));
        }
        else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value="/figure/move/random/{color}")
    public ResponseEntity<GameState> moveRandomFigure(@PathVariable("color") String color) {
        if(color != null) {
            return ResponseEntity.ok(engine.moveRandomFigure(color));
        }
        else return ResponseEntity.status(406).build();
    }

    @GetMapping(value = "/figure/move/possible/{from}")
    public ResponseEntity<List<Position>> getPossibleToPositions(@PathVariable("from") String from) {
        if(from != null) {
            return ResponseEntity.ok(engine.getPossibleMovesForPosition(engine.getBoard(), from));
        }
        else return ResponseEntity
                .status(406)
                .build();
    }
}
