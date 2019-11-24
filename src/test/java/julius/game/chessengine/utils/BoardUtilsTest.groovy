package julius.game.chessengine.utils

import julius.game.chessengine.Board
import julius.game.chessengine.Color
import julius.game.chessengine.Field
import julius.game.chessengine.Position
import julius.game.chessengine.figures.Pawn
import spock.lang.Specification

class BoardUtilsTest extends Specification {

    def "IsEnemyOnField"() {
        given:
        def board = new Board()

        when:
        def currentPlayerColor = Color.WHITE
        def enemyPlayerColor = Color.BLACK
        def enemyField = new Field(Color.BLACK, new Position('a' as char, 7))

        then:
        board.isEnemyOnField(enemyField, currentPlayerColor)
        !board.isEnemyOnField(enemyField, enemyPlayerColor)
    }

    def "MoveFigureToField"() {
        given:
        def board = new Board()
        def a3 = new Position('a' as char, 3)
        def fieldA2 = new Field(Color.WHITE, new Position('a' as char, 2))
        def fieldA3 = new Field(Color.WHITE, a3)
        def pawn = new Pawn(Color.WHITE, fieldA2)

        when:
        board.moveFigureToField(pawn, fieldA3)

        then:
        board.getFigureForPosition(a3) == pawn
    }

    def "Pawn moves to Field c6 and hits enemy Pawn on b7 from field"() {
        given:
        def board = new Board()
        def c2 = new Position('c' as char, 2)
        def c6 = new Position('c' as char, 6)
        def b7 = new Position('b' as char, 7)
        def fieldC2 = new Field(Color.WHITE, c2)
        def fieldC6 = new Field(Color.WHITE, c6)
        def fieldB7 = new Field(Color.WHITE, b7)
        def pawn = new Pawn(Color.WHITE, fieldC2)

        when:
        //Move pawn to field C6
        board.moveFigureToField(pawn, fieldC6)
        def whitePawn = board.getFigureForPosition(c6)

        //Hit Enemy Pawn from field
        whitePawn.attack(board, fieldB7)

        then:
        whitePawn.color == Color.WHITE
        whitePawn.currentField == fieldB7
        whitePawn.type == "PAWN"
        board.getFigures().size() == 19
    }

}
