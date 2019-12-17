package julius.game.chessengine.figures

import julius.game.chessengine.board.Board
import julius.game.chessengine.board.Field
import julius.game.chessengine.board.Position
import spock.lang.Specification

class QueenTest extends Specification {

    def "Move"() {
    }

    def "Attack"() {
    }

    def "GetPossibleFields"() {
        given:
        def board = new Board()
        def pawn = board.getFigureForPosition(new Position('d' as char, 2))
        def pawn2 = board.getFigureForPosition(new Position('e' as char, 2))
        def queen = board.getFigureForPosition(new Position('d' as char, 1))

        when:
        pawn.move(board, board.getFieldForPosition(new Position('d' as char, 3)))
        pawn2.move(board, board.getFieldForPosition(new Position('e' as char, 4)))
        board.logBoard()

        List<Field> possibleFields = queen.getPossibleFields(board)

        then:
        possibleFields.size() == 5
    }
}
