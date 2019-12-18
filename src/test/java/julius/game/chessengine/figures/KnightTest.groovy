package julius.game.chessengine.figures

import julius.game.chessengine.utils.Color
import julius.game.chessengine.board.Board
import julius.game.chessengine.board.Field
import julius.game.chessengine.board.Position
import spock.lang.Specification

class KnightTest extends Specification {

    def "GetPossibleFields"() {
        given:
        def board = new Board()
        def knight = board.getFigureForPosition(new Position('b' as char, 8))

        when:
        List<Field> possibleFields = knight.getPossibleFields(board)

        then:
        possibleFields.size() == 2
    }

    def "Move"() {
        given:
        def board = new Board()
        def knight = board.getFigureForPosition(new Position('b' as char, 8))
        def c6 = new Position('c' as char, 6)
        def toField = board.getFieldForPosition(c6)
        board.logBoard()

        when:
        knight.move(board, toField)
        board.logBoard()

        def isKnightThere = board.getFigureForPosition(c6)

        then:
        isKnightThere.getColor() == Color.BLACK
        isKnightThere.getType() == "KNIGHT"
        isKnightThere.getPossibleFields(board).size() == 5
    }

    def "Attack"() {
        given:
        def board = new Board()
        def knight = board.getFigureForPosition(new Position('b' as char, 8))

        def c6 = new Position('c' as char, 6)
        def d4 = new Position('d' as char, 4)
        def e2_attack = new Position('e' as char, 2)
        board.logBoard()

        when:
        def toField = board.getFieldForPosition(c6)
        knight.move(board, toField)
        knight = board.getFigureForPosition(c6)
        board.logBoard()

        def toField2 = board.getFieldForPosition(d4)
        knight.move(board, toField2)
        knight = board.getFigureForPosition(d4)
        board.logBoard()

        def attackField = board.getFieldForPosition(e2_attack)
        knight.attack(board, attackField)
        board.logBoard()

        then:
        board.getFigures().size() == 31
    }

}
