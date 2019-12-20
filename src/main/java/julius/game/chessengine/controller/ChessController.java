package julius.game.chessengine.controller;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.FrontendBoard;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.figures.Figure;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/chess")
@RequiredArgsConstructor
public class ChessController {

    private final Engine engine;

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
    public ResponseEntity<List<Field>> getAllPossibleFieldsWhite() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsWhite());
    }

    @GetMapping(value = "/field/possible/black")
    public ResponseEntity<List<Field>> getAllPossibleFieldsBlack() {
        return ResponseEntity.ok(engine.getAllPossibleMoveFieldsBlack());
    }

    @GetMapping(value = "/figure")
    public ResponseEntity<List<Figure>> getFigures() {
        return ResponseEntity.ok(engine.getBoard().getFigures());
    }

    @GetMapping(value = "/figure/frontend")
    public ResponseEntity<FrontendBoard> getFiguresFrontend() {return ResponseEntity.ok(engine.translateBoardToFrontend());}

    @PatchMapping(value="/figure/move/{from}/{to}")
    public ResponseEntity<?> moveFigure(@PathVariable("from") String from,
                                        @PathVariable("to") String to) {
        if(from != null && to != null) {
            engine.moveFigure(from, to);
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.status(406).build();
    }
}
