package julius.game.chessengine.player;

import julius.game.chessengine.Field;

public class PlayerWhite implements Player {

    @Override
    public void movePawn(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

    @Override
    public void moveRook(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

    @Override
    public void moveKnight(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

    @Override
    public void moveBishop(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

    @Override
    public void moveQueen(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

    @Override
    public void moveKing(Field from, Field to) {
        to.setFigure(from.getFigure());
        from.setFigure(null);
    }

}
