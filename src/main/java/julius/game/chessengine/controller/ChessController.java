package julius.game.chessengine.controller;

import julius.game.chessengine.ai.AI;
import julius.game.chessengine.ai.MoveAndScore;
import julius.game.chessengine.board.*;
import julius.game.chessengine.engine.Engine;
import julius.game.chessengine.engine.GameState;
import julius.game.chessengine.engine.GameStateEnum;
import julius.game.chessengine.utils.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static julius.game.chessengine.board.MoveHelper.convertIndexToString;
import static julius.game.chessengine.board.MoveHelper.convertStringToIndex;

@Log4j2
@Controller
@RequestMapping(value = "/chess")
@RequiredArgsConstructor
public class ChessController {

    private final AI ai;
    private final Engine engine;

    @GetMapping(value = "/score")
    public ResponseEntity<Score> getScore() {
        return ResponseEntity.ok(engine.getGameState().getScore());
    }

    @PutMapping(value = "/reset")
    public ResponseEntity<?> resetBoard() {
        ai.reset();
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/fen")
    public ResponseEntity<?> setBoardToFEN(@RequestParam("fen") String fen) {
        engine.importBoardFromFen(fen);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/autoplay")
    public ResponseEntity<?> autoplay() {
        ai.startAutoPlay(true, true);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/autoplay/timelimit/{timeLimit}")
    public ResponseEntity<?> autoplaySetTimelimit(@PathVariable("timeLimit") long timeLimit)  {
        ai.setTimeLimit(timeLimit);
        log.debug("setting to: " + timeLimit);
        return ResponseEntity.ok().build();
    }
    @PatchMapping(value = "/autoplay/{color}")
    public ResponseEntity<?> calculateMoveForColor(@PathVariable("color") String color) {
        if (color != null) {
            log.debug(color);
            ai.startAutoPlay(color.equalsIgnoreCase("WHITE"), color.equalsIgnoreCase("BLACK"));
            return ResponseEntity.ok().build();
        } else return ResponseEntity.status(406).build();
    }

    @GetMapping(value = "/autoplay/lastMove")
    public ResponseEntity<ApiMove> getLastMove() {
        GameStateEnum state = ai.getMainEngine().getGameState().getState();
        int lastMove = ai.getMainEngine().getLine().getLast();
        int fromIndex = MoveHelper.deriveFromIndex(lastMove);
        int toIndex = MoveHelper.deriveToIndex(lastMove);

        ApiMove move = new ApiMove(state, convertIndexToString(fromIndex),convertIndexToString(toIndex));
        return ResponseEntity.ok(move);
    }

    @GetMapping(value = "/undo")
    public ResponseEntity<?> undoLastMove() {
        engine.undoLastMove();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/field/possible/white")
    public ResponseEntity<List<Move>> getAllPossibleFieldsWhite() {
        MoveList moves = engine.getAllLegalMoves();
        List<Move> restApiMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            restApiMoves.add(Move.convertIntToMove(moves.getMove(i)));
        }

        return ResponseEntity.ok(restApiMoves);
    }

    @GetMapping(value = "/field/possible/black")
    public ResponseEntity<List<Move>> getAllPossibleFieldsBlack() {
        MoveList moves = engine.getAllLegalMoves();
        List<Move> restApiMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            restApiMoves.add(Move.convertIntToMove(moves.getMove(i)));
        }

        return ResponseEntity.ok(restApiMoves);
    }

    @GetMapping(value = "/figure/frontend")
    public ResponseEntity<FEN> getFiguresFrontend() {
        return ResponseEntity.ok(engine.translateBoardToFen());
    }

    @PatchMapping(value = "/figure/move/{from}/{to}")
    public ResponseEntity<GameState> moveFigure(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        if (from != null && to != null) {
            //TODO implement promotion
            GameState state = engine.moveFigure(convertStringToIndex(from), convertStringToIndex(to), 5);
            ai.updateBoardStateHash();
            return ResponseEntity.ok(state);
        } else return ResponseEntity.status(406).build();
    }
    @GetMapping(value = "/figure/move/possible/{from}")
    public ResponseEntity<List<Position>> getPossibleToPositions(@PathVariable("from") String from) {
        if (from != null) {
            return ResponseEntity.ok(engine.getPossibleMovesForPosition(convertStringToIndex(from)));
        } else return ResponseEntity
                .status(406)
                .build();
    }

    @GetMapping(value = "/state")
    public ResponseEntity<BoardState> getBoardState() {
        List<MoveAndScore> moveAndScores = ai.getCalculatedLine();
        GameState gameState = ai.getMainEngine().getGameState();

        BoardState boardState = new BoardState();
        boardState.setGameState(gameState);

        if (!moveAndScores.isEmpty()) {
            String moves = moveAndScores.stream()
                    .map(ms -> Move.convertIntToMove(ms.getMove()).toString())
                    .collect(Collectors.joining(", "));
            boardState.setMove(moves);

            double lastScore = moveAndScores.get(moveAndScores.size() - 1).getScore();
            boardState.setScore(lastScore);
        }

        return ResponseEntity.ok(boardState);
    }

}
