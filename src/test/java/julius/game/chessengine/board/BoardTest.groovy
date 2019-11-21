package julius.game.chessengine.board

import julius.game.chessengine.Board
import julius.game.chessengine.Field
import julius.game.chessengine.Position
import julius.game.chessengine.figures.Figure
import julius.game.chessengine.figures.Rook
import spock.lang.Specification

class BoardTest extends Specification {

    def "checkAmountOfFiguresAfterInit"() {
        given:
        Board board = new Board()

        when:
        List<Figure> figures = board.getFigures()

        then:
        figures.size() == 20
    }

    def "fieldTest"() {
        when:
        Field field = new Field("Black", new Position('a' as char,1), null)
        Field field2 = new Field("Black", new Position('a' as char,2), new Rook("white"))

        then:
        assert field.isEmptyField()
        assert !field2.isEmptyField()
    }
}
