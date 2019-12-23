package julius.game.chessengine.engine;

import julius.game.chessengine.board.Field;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MoveField {
    private final Field fromField;
    private final Field toField;
}
