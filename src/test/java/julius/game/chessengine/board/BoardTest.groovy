package julius.game.chessengine.board


import julius.game.chessengine.utils.Color
import julius.game.chessengine.figures.Pawn
import julius.game.chessengine.generator.FieldGenerator
import spock.lang.Specification

class BoardTest extends Specification {

    def fieldGenerator = new FieldGenerator()

    def "checkAmountOfFiguresAndFieldsAfterInit"() {
        given:
        def board = new Board()

        when:
        def figures = board.getFigures()
        def fields = board.getFields()

        then:
        figures.size() == 32
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
        figure.getColor() == Color.WHITE
    }

    def "showPossibleMovesOfPawn"() {
        given:
        def board = new Board()
        def a2 = new Field(Color.WHITE, new Position('a' as char, 2))
        def pawn = new Pawn(Color.WHITE, a2)

        when:
        def fields = pawn.getPossibleFields(board)

        then:
        fields == [new Field(Color.BLACK, new Position('a' as char, 3)),
                   new Field(Color.WHITE, new Position('a' as char, 4))]

    }

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

    def "MovePawnToField"() {
        given:
        def board = new Board()
        def a2 = new Position('a' as char, 2)
        def a3 = new Position('a' as char, 3)
        def fieldA3 =  board.getFieldForPosition(a3)
        def pawn = board.getFigureForPosition(a2)
        board.logBoard()

        when:
        pawn.move(board, fieldA3)
        board.logBoard()

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
        board.logBoard()

        when:
        //Move pawn to field C6
        board.moveFigureToField(pawn, fieldC6)
        def whitePawn = board.getFigureForPosition(c6)
        board.logBoard()

        //Hit Enemy Pawn from field
        whitePawn.attack(board, fieldB7)
        board.logBoard()

        then:
        whitePawn.color == Color.WHITE
        whitePawn.currentField == fieldB7
        whitePawn.type == "PAWN"
        board.getFigures().size() == 31
    }

    def "Rook cant move in the beginning"() {
        given:
        def board = new Board()
        def rook = board.getFigureForPosition(new Position('a' as char, 1))

        when:
        def possibleFields = rook.getPossibleFields(board)

        then:
        possibleFields == []
    }

    def "checkIfBoardIsGeneratedCorrect"() {
        when:
        def boardFields = fieldGenerator.generateFields()

        then:
        boardFields ==  [
                new Field(Color.BLACK, new Position('a' as char, 1)),
                new Field(Color.WHITE, new Position('a' as char, 2)),
                new Field(Color.BLACK, new Position('a' as char, 3)),
                new Field(Color.WHITE, new Position('a' as char, 4)),
                new Field(Color.BLACK, new Position('a' as char, 5)),
                new Field(Color.WHITE, new Position('a' as char, 6)),
                new Field(Color.BLACK, new Position('a' as char, 7)),
                new Field(Color.WHITE, new Position('a' as char, 8)),

                new Field(Color.WHITE, new Position('b' as char, 1)),
                new Field(Color.BLACK, new Position('b' as char, 2)),
                new Field(Color.WHITE, new Position('b' as char, 3)),
                new Field(Color.BLACK, new Position('b' as char, 4)),
                new Field(Color.WHITE, new Position('b' as char, 5)),
                new Field(Color.BLACK, new Position('b' as char, 6)),
                new Field(Color.WHITE, new Position('b' as char, 7)),
                new Field(Color.BLACK, new Position('b' as char, 8)),

                new Field(Color.BLACK, new Position('c' as char, 1)),
                new Field(Color.WHITE, new Position('c' as char, 2)),
                new Field(Color.BLACK, new Position('c' as char, 3)),
                new Field(Color.WHITE, new Position('c' as char, 4)),
                new Field(Color.BLACK, new Position('c' as char, 5)),
                new Field(Color.WHITE, new Position('c' as char, 6)),
                new Field(Color.BLACK, new Position('c' as char, 7)),
                new Field(Color.WHITE, new Position('c' as char, 8)),

                new Field(Color.WHITE, new Position('d' as char, 1)),
                new Field(Color.BLACK, new Position('d' as char, 2)),
                new Field(Color.WHITE, new Position('d' as char, 3)),
                new Field(Color.BLACK, new Position('d' as char, 4)),
                new Field(Color.WHITE, new Position('d' as char, 5)),
                new Field(Color.BLACK, new Position('d' as char, 6)),
                new Field(Color.WHITE, new Position('d' as char, 7)),
                new Field(Color.BLACK, new Position('d' as char, 8)),

                new Field(Color.BLACK, new Position('e' as char, 1)),
                new Field(Color.WHITE, new Position('e' as char, 2)),
                new Field(Color.BLACK, new Position('e' as char, 3)),
                new Field(Color.WHITE, new Position('e' as char, 4)),
                new Field(Color.BLACK, new Position('e' as char, 5)),
                new Field(Color.WHITE, new Position('e' as char, 6)),
                new Field(Color.BLACK, new Position('e' as char, 7)),
                new Field(Color.WHITE, new Position('e' as char, 8)),

                new Field(Color.WHITE, new Position('f' as char, 1)),
                new Field(Color.BLACK, new Position('f' as char, 2)),
                new Field(Color.WHITE, new Position('f' as char, 3)),
                new Field(Color.BLACK, new Position('f' as char, 4)),
                new Field(Color.WHITE, new Position('f' as char, 5)),
                new Field(Color.BLACK, new Position('f' as char, 6)),
                new Field(Color.WHITE, new Position('f' as char, 7)),
                new Field(Color.BLACK, new Position('f' as char, 8)),

                new Field(Color.BLACK, new Position('g' as char, 1)),
                new Field(Color.WHITE, new Position('g' as char, 2)),
                new Field(Color.BLACK, new Position('g' as char, 3)),
                new Field(Color.WHITE, new Position('g' as char, 4)),
                new Field(Color.BLACK, new Position('g' as char, 5)),
                new Field(Color.WHITE, new Position('g' as char, 6)),
                new Field(Color.BLACK, new Position('g' as char, 7)),
                new Field(Color.WHITE, new Position('g' as char, 8)),

                new Field(Color.WHITE, new Position('h' as char, 1)),
                new Field(Color.BLACK, new Position('h' as char, 2)),
                new Field(Color.WHITE, new Position('h' as char, 3)),
                new Field(Color.BLACK, new Position('h' as char, 4)),
                new Field(Color.WHITE, new Position('h' as char, 5)),
                new Field(Color.BLACK, new Position('h' as char, 6)),
                new Field(Color.WHITE, new Position('h' as char, 7)),
                new Field(Color.BLACK, new Position('h' as char, 8))
        ]
    }
}
