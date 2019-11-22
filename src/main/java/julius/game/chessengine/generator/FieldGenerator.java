package julius.game.chessengine.generator;

import julius.game.chessengine.Color;
import julius.game.chessengine.Field;
import julius.game.chessengine.Position;

import java.util.ArrayList;
import java.util.List;

public class FieldGenerator {

    public List<Field> generateFields() {
        List<Field> fields = new ArrayList<>();
        for(char x = 'a'; x <= 'h'; x++) {
            if(x % 2 == 0) {
                for (int y = 1; y <= 8; y++) {
                    if (y % 2 == 0) {
                        fields.add(new Field(Color.BLACK, new Position(x, y)));
                    } else {
                        fields.add(new Field(Color.WHITE, new Position(x, y)));
                    }
                }
            }
            else {
                for (int y = 1; y <= 8; y++) {
                    if (y % 2 == 0) {
                        fields.add(new Field(Color.WHITE, new Position(x, y)));
                    } else {
                        fields.add(new Field(Color.BLACK, new Position(x, y)));
                    }
                }
            }
        }
        return fields;
    }
}
