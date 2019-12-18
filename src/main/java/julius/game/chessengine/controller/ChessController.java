package julius.game.chessengine.controller;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.figures.Figure;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping(value = "/chess")
@RequiredArgsConstructor
public class ChessController {

    private final Engine engine;

    @GetMapping(value = "/field")
    public ResponseEntity<List<Field>> getFields() {
        return ResponseEntity.ok(engine.getBoard().getFields());
    }

    @GetMapping(value = "/figure")
    public ResponseEntity<List<Figure>> getFigures() {
        return ResponseEntity.ok(engine.getBoard().getFigures());
    }

}
