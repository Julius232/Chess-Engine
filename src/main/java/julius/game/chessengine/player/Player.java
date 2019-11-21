package julius.game.chessengine.player;

import julius.game.chessengine.Field;

public interface Player {

    void movePawn(Field from, Field to);

    void moveRook(Field from, Field to);

    void moveKnight(Field from, Field to);

    void moveBishop(Field from, Field to);

    void moveQueen(Field from, Field to);

    void moveKing(Field from, Field to);

}
