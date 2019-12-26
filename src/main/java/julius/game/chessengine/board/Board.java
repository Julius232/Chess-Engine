package julius.game.chessengine.board;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.figures.King;
import julius.game.chessengine.figures.Rook;
import julius.game.chessengine.generator.FieldGenerator;
import julius.game.chessengine.generator.FigureGenerator;
import julius.game.chessengine.utils.Color;
import julius.game.chessengine.utils.Score;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Log4j2
@Component
public class Board {
    private Score score = new Score();

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator = new FieldGenerator();
    private final FigureGenerator figureGenerator = new FigureGenerator(this);

    private final List<Field> fields = fieldGenerator.generateFields();
    private List<Figure> figures;

    public Board(){
        this.figures = figureGenerator.initializeFigures();
    }

    public Board(String FEN, Score score) {
        this.figures = figureGenerator.getFigures(FEN);
        this.score = score;
    }

    //FIGURE OPERATIONS

    public Figure getFigureForPosition(Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getX() + position.getY()));
    }

    public Figure getFigureForString(String position) {
        if(position.length() == 2) {
            return getFigureForPosition(new Position(position.charAt(0), Character.getNumericValue(position.charAt(1))));
        }
        else throw new IllegalStateException("Position " + position + " is not valid");
    }

    public boolean isEmptyField(Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

    public List<Field> getAllOccupiedFields() {
        return figures.stream()
                .filter(figure -> isOccupiedField(figure.getCurrentField()))
                .map(Figure::getCurrentField)
                .collect(Collectors.toList());
    }

    public boolean isEnemyOnField(Field field, String currentPlayerColor) {
        return figures.stream()
                .filter(figure -> field.equals(figure.getCurrentField()))
                .anyMatch(figure -> ! currentPlayerColor.equals(figure.getColor()));
    }

    public List<Field> getAllEnemyFields(String currentPlayerColor) {
        return figures.stream()
                .filter(figure -> isEnemyOnField(figure.getCurrentField(), currentPlayerColor))
                .map(Figure::getCurrentField)
                .collect(Collectors.toList());
    }

    public List<King> getKings() {
        return figures.stream()
                .filter(figure -> figure instanceof King)
                .map(king -> (King) king)
                .collect(Collectors.toList());
    }

    public boolean isPlayerInStateCheck(String color) {
        return getKings().stream()
                .filter(king -> color.equals(king.getColor()))
                .anyMatch(King::isInStateCheck);
    }

    //FIELD OPERATIONS

    public Field getFieldForPosition(Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElse(new Field("offsideTheBoard", position));
    }

    public Field getFieldForString(String position) {
        if(position.length() == 2) {
            return getFieldForPosition(new Position(position.charAt(0), Character.getNumericValue(position.charAt(1))));
        }
        else throw new IllegalStateException("Position " + position + " is not valid");
    }

    public List<Field> getAllEmptyFields() {
        return fields.stream()
                .filter(this::isEmptyField)
                .collect(Collectors.toList());
    }

    public boolean isOccupiedField(Field field) {
        return !isEmptyField(field);
    }

    //X-AXIS OPERATIONS
    public List<Field> getAllFieldsXAxis(int y) {
        return fields.stream()
                .filter(field -> y == field.getPosition().getY())
                .collect(Collectors.toList());
    }

    public List<Field> getEmptyFieldsXAxis(int y) {
        return getAllFieldsXAxis(y).stream()
                .filter(this::isEmptyField)
                .collect(Collectors.toList());
    }

    public List<Field> getOccupiedFieldsXAxis(int y) {
        return getAllFieldsXAxis(y).stream()
                .filter(this::isOccupiedField)
                .collect(Collectors.toList());
    }

    public List<Field> getEnemyFieldsXAxis(int y, String currentPlayerColor) {
        return getAllFieldsXAxis(y).stream()
                .filter(field -> isEnemyOnField(field, currentPlayerColor))
                .collect(Collectors.toList());
    }

    public List<Field> getPossibleFieldsXAxis(Figure figure) {
        int y = figure.getPosY();
        List<Field> possibleFields = new ArrayList<>();
        List<Field> emptyFieldsXAxis = getEmptyFieldsXAxis(y);

        for(char x = (char)(figure.getPosX() + 1); x <= 'h'; x++) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if(emptyFieldsXAxis.contains(nextField)) {
                possibleFields.add(nextField);
            }
            else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        for(char x = (char)(figure.getPosX() - 1); x >= 'a'; x--) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if(emptyFieldsXAxis.contains(nextField)) {
                possibleFields.add(nextField);
            }
            else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        return possibleFields;
    }

    //Y-AXIS OPERATIONS
    public List<Field> getAllFieldsYAxis(char x) {
        return fields.stream()
                .filter(field -> x == field.getPosition().getX())
                .collect(Collectors.toList());
    }

    public List<Field> getEmptyFieldsYAxis(char x) {
        return getAllFieldsYAxis(x).stream()
                .filter(this::isEmptyField)
                .collect(Collectors.toList());
    }

    public List<Field> getOccupiedFieldsYAxis(char x) {
        return getAllFieldsYAxis(x).stream()
                .filter(this::isOccupiedField)
                .collect(Collectors.toList());
    }

    public List<Field> getEnemyFieldsYAxis(char x, String currentPlayerColor) {
        return getAllFieldsYAxis(x).stream()
                .filter(field -> isEnemyOnField(field, currentPlayerColor))
                .collect(Collectors.toList());
    }

    public List<Field> getPossibleFieldsYAxis(Figure figure) {
        char x = figure.getPosX();
        List<Field> possibleFields = new ArrayList<>();
        List<Field> emptyFieldsYAxis = getEmptyFieldsYAxis(x);

        for (int y = figure.getPosY() + 1; y <= 8; y++) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if (emptyFieldsYAxis.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        for (int y = figure.getPosY() - 1; y >= 1; y--) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if (emptyFieldsYAxis.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        return possibleFields;
    }

    //Diagonal LDRU Operations
    public List<Field> getAllFieldsDiagonalLDRU(Position position) {
        List<Field> allDiagonalLDRUfields = new ArrayList<>();

        char posXCountUp = position.getX();
        int posYCountUp = position.getY();

        while(posXCountUp <= 'h' && posYCountUp <= 8) {
            posXCountUp++;
            posYCountUp++;
            allDiagonalLDRUfields.add(getFieldForPosition(new Position(posXCountUp, posYCountUp)));
        }

        char posXCountDown = position.getX();
        int posYCountDown = position.getY();

        while(posXCountDown >= 'a' && posYCountDown >= 1) {
            posXCountDown--;
            posYCountDown--;
            allDiagonalLDRUfields.add(getFieldForPosition(new Position(posXCountDown, posYCountDown)));
        }

        return allDiagonalLDRUfields;
    }

    public List<Field> getEmptyFieldsDiagonalLDRU(Position position) {
        return getAllFieldsDiagonalLDRU(position).stream()
                .filter(this::isEmptyField)
                .collect(Collectors.toList());
    }

    public List<Field> getPossibleFieldsDiagonalLDRU(Figure figure) {
        List<Field> possibleFields = new ArrayList<>();
        List<Field> emptyFields = getEmptyFieldsDiagonalLDRU(figure.getCurrentPosition());

        char posXCountUp = figure.getPosX();
        int posYCountUp = figure.getPosY();

        while(posXCountUp + 1 <= 'h' && posYCountUp + 1 <= 8) {
            posXCountUp++;
            posYCountUp++;
            Field nextField = getFieldForPosition(new Position(posXCountUp, posYCountUp));
            if (emptyFields.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        char posXCountDown = figure.getPosX();
        int posYCountDown = figure.getPosY();

        while(posXCountDown - 1 >= 'a' && posYCountDown - 1 >= 1) {
            posXCountDown--;
            posYCountDown--;
            Field nextField = getFieldForPosition(new Position(posXCountDown, posYCountDown));
            if (emptyFields.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        return possibleFields;
    }

    //Diagonal RDLU Operations
    public List<Field> getAllFieldsDiagonalRDLU(Position position) {
        List<Field> allDiagonalRDLUfields = new ArrayList<>();

        char posXCountUp = position.getX();
        int posYCountUp = position.getY();

        while(posXCountUp - 1 >= 'a' && posYCountUp + 1 <= 8) {
            posXCountUp--;
            posYCountUp++;
            allDiagonalRDLUfields.add(getFieldForPosition(new Position(posXCountUp, posYCountUp)));
        }

        char posXCountDown = position.getX();
        int posYCountDown = position.getY();

        while(posXCountDown + 1 <= 'h' && posYCountDown - 1 >= 1) {
            posXCountDown++;
            posYCountDown--;
            allDiagonalRDLUfields.add(getFieldForPosition(new Position(posXCountDown, posYCountDown)));
        }

        return allDiagonalRDLUfields;
    }

    public List<Field> getEmptyFieldsDiagonalRDLU(Position position) {
        return getAllFieldsDiagonalRDLU(position).stream()
                .filter(this::isEmptyField)
                .collect(Collectors.toList());
    }

    public List<Field> getPossibleFieldsDiagonalRDLU(Figure figure) {
        List<Field> possibleFields = new ArrayList<>();
        List<Field> emptyFields = getEmptyFieldsDiagonalRDLU(figure.getCurrentPosition());

        char posXCountDown = figure.getPosX();
        int posYCountUp = figure.getPosY();

        while(posXCountDown - 1 >= 'a' && posYCountUp + 1 <= 8) {
            posXCountDown--;
            posYCountUp++;
            Field nextField = getFieldForPosition(new Position(posXCountDown, posYCountUp));
            if (emptyFields.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        char posXCountUp = figure.getPosX();
        int posYCountDown = figure.getPosY();

        while(posXCountUp + 1 <= 'h' && posYCountDown - 1 >= 1) {
            posXCountUp++;
            posYCountDown--;
            Field nextField = getFieldForPosition(new Position(posXCountUp, posYCountDown));
            if (emptyFields.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                }
                break;
            }
        }

        return possibleFields;
    }

    //MOVE && ATTACK OPERATIONS
    public void hitFigureFromBoard(Figure movingFigure, Field toField) {
        figures = figures.stream()
                .filter(figure -> !toField.equals(figure.getCurrentField()))
                .collect(Collectors.toList());
        moveFigureToField(movingFigure, toField);
    }

    public List<Figure> moveFigureToField(Figure movingFigure, Field toField) {
        for(Figure figure : figures) {
            if(movingFigure.getCurrentPosition().equals(figure.getCurrentPosition())) {
                figure.setCurrentField(toField);
                getKings().forEach(king -> king.setInStateCheck(king.checkState(this)));
            }
        }
        return figures;
    }

    public boolean isCastlingPossibleQueenSide(String playerColor) {
        int yAxis = playerColor.equals(Color.WHITE) ? 1 : 8;
        boolean isRookFieldOccupied;
        boolean isEmpty0;
        boolean isEmpty1;
        boolean isEmpty2;
        boolean isKingFieldOccupied;

        isRookFieldOccupied = isOccupiedField(getFieldForPosition(new Position('a', yAxis)));
        isEmpty0 = isEmptyField(getFieldForPosition(new Position('b', yAxis)));
        isEmpty1 = isEmptyField(getFieldForPosition(new Position('c', yAxis)));
        isEmpty2 = isEmptyField(getFieldForPosition(new Position('d', yAxis)));
        isKingFieldOccupied = isOccupiedField(getFieldForPosition(new Position('e', yAxis)));
        if(isRookFieldOccupied && isEmpty0 && isEmpty1 && isEmpty2 && isKingFieldOccupied) {
            Figure isKing = getFigureForPosition(new Position('e', yAxis));
            Figure isRook = getFigureForPosition(new Position('a', yAxis));
            if(isKing.getType().equals("KING") && isRook.getType().equals("ROOK")) {
                King king = (King) isKing;
                Rook rook = (Rook) isRook;
                return !king.isHasMoved() && !rook.isHasMoved();
            }
        }
        return false;
    }

    public boolean isCastlingPossibleKingSide(String playerColor) {
        int yAxis = playerColor.equals(Color.WHITE) ? 1 : 8;
        boolean isRookFieldOccupied;
        boolean isEmpty0;
        boolean isEmpty1;
        boolean isKingFieldOccupied;

        isRookFieldOccupied = isOccupiedField(getFieldForPosition(new Position('h', yAxis)));
        isEmpty0 = isEmptyField(getFieldForPosition(new Position('g', yAxis)));
        isEmpty1 = isEmptyField(getFieldForPosition(new Position('f', yAxis)));
        isKingFieldOccupied = isOccupiedField(getFieldForPosition(new Position('e', yAxis)));
        if(isRookFieldOccupied && isEmpty0 && isEmpty1 && isKingFieldOccupied) {
            Figure isKing = getFigureForPosition(new Position('e', yAxis));
            Figure isRook = getFigureForPosition(new Position('h', yAxis));
            if(isKing.getType().equals("KING") && isRook.getType().equals("ROOK")) {
                King king = (King) isKing;
                Rook rook = (Rook) isRook;
                return !king.isHasMoved() && !rook.isHasMoved();
            }
        }
        return false;
    }

    public void logBoard() {
        StringBuilder logBoard = new StringBuilder();
        int counter = 1;
        for(int y = 8; y >= 1; y--) {
            logBoard.append(y).append(": ");
            for (char x = 'a'; x <= 'h'; x++) {
                Field logField = getFieldForPosition(new Position(x,y));

                if(isOccupiedField(logField)) {
                    Figure logFigure = getFigureForPosition(new Position(x, y));
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("PAWN")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265F)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("PAWN")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2659)));
                        }
                    }
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("ROOK")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265C)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("ROOK")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2656)));
                        }
                    }
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("KNIGHT")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265E)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("KNIGHT")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2658)));
                        }
                    }
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("BISHOP")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265D)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("BISHOP")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2657)));
                        }
                    }
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("QUEEN")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265B)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("QUEEN")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2655)));
                        }
                    }
                    if(logFigure.getColor().equals(Color.BLACK)) {
                        if (logFigure.getType().equals("KING")) {
                            logBoard.append(String.valueOf(Character.toChars(0x265A)));
                        }
                    }
                    else {
                        if (logFigure.getType().equals("KING")) {
                            logBoard.append(String.valueOf(Character.toChars(0x2654)));
                        }
                    }
                }
                else if (logField.getColor().equals(Color.BLACK)) {
                    logBoard.append(String.valueOf(Character.toChars(0x25AE)));
                }
                else {
                    logBoard.append(String.valueOf(Character.toChars(0x25AF)));
                }
                if (counter % 8 == 0) {
                    log.info(logBoard.toString());
                    logBoard = new StringBuilder();
                }
                counter++;
            }
        }
        log.info("   --------------");
    }
}
