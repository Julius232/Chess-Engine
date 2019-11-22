package julius.game.chessengine.board

import julius.game.chessengine.Board
import julius.game.chessengine.Position
import spock.lang.Specification

class BoardTest extends Specification {

    def static final WHITE = "white"
    def static final BLACK = "black"

    def "checkAmountOfFiguresAndFieldsAfterInit"() {
        given:
        def board = new Board()

        when:
        def figures = board.getFigures()
        def fields = board.getFields()

        then:
        figures.size() == 20
        fields.size() == 64
    }

    def "isFigureOnFieldTest"() {
        given:
        def emptyPosition = new Position('a' as char, 3)
        def rookPosition = new Position('a' as char, 1)

        when:
        def board = new Board()

        then:
        board.isEmptyField(board.getFieldForPosition(emptyPosition))
        !board.isEmptyField(board.getFieldForPosition(rookPosition))
    }

    def "getFigureForPositionTest"() {
        given:
        def board = new Board()
        def whiteRookPosition = new Position('a' as char, 1)

        when:
        def figure = board.getFigureForPosition(whiteRookPosition)

        then:
        figure.getType() == "ROOK"
        figure.getColor() == WHITE
    }
}
