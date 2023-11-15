package julius.game.chessengine.controller;

import julius.game.chessengine.ai.AI;
import julius.game.chessengine.board.FEN;
import julius.game.chessengine.board.Move;
import julius.game.chessengine.board.Position;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static julius.game.chessengine.board.Position.convertStringToPosition;

@Log4j2
@Controller
@RequestMapping(value = "/chess")
@RequiredArgsConstructor
public class ChessController {

    private final AI ai;
    private final Engine engine;

    @GetMapping(value = "/score")
    public ResponseEntity<Score> getScore() {
        return ResponseEntity.ok(engine.getScore());
    }

    @PutMapping(value = "/reset")
    public ResponseEntity<?> resetBoard() {
        engine.startNewGame();
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/fen")
    public ResponseEntity<?> setBoardToFEN(@RequestParam("fen") String fen) {
        engine.importBoardFromFen(fen);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/autoplay")
    public ResponseEntity<?> autoplay() throws InterruptedException {
        ai.startAutoPlay();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/undo")
    public ResponseEntity<?> undoLastMove() {
        engine.undoLastMove();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/field/possible/white")
    public ResponseEntity<List<Move>> getAllPossibleFieldsWhite() {
        return ResponseEntity.ok(engine.getAllLegalMoves());
    }

    @GetMapping(value = "/field/possible/black")
    public ResponseEntity<List<Move>> getAllPossibleFieldsBlack() {
        return ResponseEntity.ok(engine.getAllLegalMoves());
    }

    @GetMapping(value = "/figure/frontend")
    public ResponseEntity<FEN> getFiguresFrontend() {
        return ResponseEntity.ok(engine.translateBoardToFen());
    }

    @PatchMapping(value = "/figure/move/{from}/{to}")
    public ResponseEntity<GameState> moveFigure(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        if (from != null && to != null) {
            GameState state = engine.moveFigure(convertStringToPosition(from), convertStringToPosition(to));
            return ResponseEntity.ok(state);
        } else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value = "/figure/move/intelligent/{color}")
    public ResponseEntity<GameState> calculateMoveForColor(@PathVariable("color") String color) {
        if (color != null) {
            return ResponseEntity.ok(ai.performMove());
        } else return ResponseEntity.status(406).build();
    }

    @PatchMapping(value = "/figure/move/random/{color}")
    public ResponseEntity<GameState> moveRandomFigure(@PathVariable("color") String color) {
        if (color != null) {
            return ResponseEntity.ok(engine.moveRandomFigure(color.equals("WHITE")));
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

    @GetMapping(value = "/line")
    public ResponseEntity<List<String>> getCalculatedLine() {
        List<String> calculatedLine = ai.getCalculatedLine()
                .stream()
                .map(Move::toString)
                .collect(Collectors.toList()); // Assuming this returns List<String>
        return ResponseEntity.ok(calculatedLine);
    }

}
