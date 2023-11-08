package julius.game.chessengine.board


import julius.game.chessengine.utils.Color


import spock.lang.Specification

class BoardTest extends Specification {



    def "checkAmountOfFiguresAndFieldsAfterInit"() {
        given:
        def board = new BitBoard()

        when:
        def figures = board.getFigures()

        then:
        figures.size() == 32
    }

}
