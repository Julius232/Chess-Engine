package julius.game.chessengine.engine;

import julius.game.chessengine.board.Board;
import julius.game.chessengine.board.Field;
import julius.game.chessengine.board.Position;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@RequiredArgsConstructor
public class MoveField {
    private final Field fromField;
    private final Field toField;

    public MoveField(Board board, String move) {
        String[] string = StringUtils.split(move, "-");
        this.fromField = board.getFieldForString(string[0]);
        this.toField = board.getFieldForString(string[1]);
    }

    public String fromPositionToString() {
        return fromField.positionToString();
    }

    public String toPositionToString() {
        return toField.positionToString();
    }

    public String toString() {
        return fromPositionToString() + "-" + toPositionToString();
    }

    public Position getFromPosition() {
        return fromField.getPosition();
    }

    public Position getToPosition() {
        return toField.getPosition();
    }

}
