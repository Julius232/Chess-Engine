package julius.game.chessengine.engine;

import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MoveField {
    private final Field fromField;
    private final Field toField;

    public String fromPositionToString() {
        return fromField.positionToString();
    }

    public String toPositionToString() {
        return toField.positionToString();
    }

    public Position getFromPosition() {
        return fromField.getPosition();
    }

    public Position getToPosition() {
        return toField.getPosition();
    }
}
