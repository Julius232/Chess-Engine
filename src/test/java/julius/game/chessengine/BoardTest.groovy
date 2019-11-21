package julius.game.chessengine

import julius.game.chessengine.figures.Figure
import julius.game.chessengine.figures.Rook
import spock.lang.Specification

class BoardTest extends Specification {

    def "checkAmountOfFiguresAfterInit"() {
        given:
        Board board = new Board()

        when:
        Set<Figure> figures = board.getFigures()

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
