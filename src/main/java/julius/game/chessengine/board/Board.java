package julius.game.chessengine.board;

import julius.game.chessengine.figures.Figure;
import julius.game.chessengine.generator.FieldGenerator;
import julius.game.chessengine.generator.FigureGenerator;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Board {

    private final static String WHITE = "white";
    private final static String BLACK = "black";

    private final FieldGenerator fieldGenerator;
    private final FigureGenerator figureGenerator;

    private final List<Field> fields;
    private List<Figure> figures;

    public Board(){
        this.fieldGenerator = new FieldGenerator();
        this.fields = fieldGenerator.generateFields();

        this.figureGenerator = new FigureGenerator(this);
        this.figures = figureGenerator.initializeFigures();
    }

    //FIGURE OPERATIONS

    public void addFigure(Figure figure) {
        this.figures.add(figure);
    }

    public Figure getFigureForPosition(Position position) {
        return figures.stream()
                .filter(figure -> position.equals(figure.getCurrentField().getPosition()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Figure at Position: " +
                        position.getXAchse() + position.getYAchse()));
    }

    //FIELD OPERATIONS

    public Field getFieldForPosition(Position position) {
        return fields.stream()
                .filter(field -> field.getPosition().equals(position))
                .findAny()
                .orElse(new Field("offsideTheBoard", position));
    }

    public boolean isEmptyField(Field field) {
        return figures.stream()
                .noneMatch(figure -> field.equals(figure.getCurrentField()));
    }

    public List<Field> getAllEmptyFields() {
        return figures.stream()
                .filter(figure -> isEmptyField(figure.getCurrentField()))
                .map(Figure::getCurrentField)
                .collect(Collectors.toList());
    }

    public boolean isOccupiedField(Field field) {
        return !isEmptyField(field);
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

    //X-AXIS OPERATIONS

    public List<Field> getAllFieldsXAxis(int y) {
        return fields.stream()
                .filter(field -> y == field.getPosition().getYAchse())
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
                    break;
                }
                else {
                    break;
                }
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
                    break;
                }
                else {
                    break;
                }
            }
        }

        return possibleFields;
    }

    //Y-AXIS OPERATIONS

    public List<Field> getAllFieldsYAxis(char x) {
        return fields.stream()
                .filter(field -> x == field.getPosition().getXAchse())
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

        for (int y = figure.getPosY() + 1; x <= 8; x++) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if (emptyFieldsYAxis.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                    break;
                } else {
                    break;
                }
            }
        }

        for (int y = figure.getPosY() - 1; x >= 1; x--) {
            Field nextField = getFieldForPosition(new Position(x, y));
            if (emptyFieldsYAxis.contains(nextField)) {
                possibleFields.add(nextField);
            } else {
                if (isEnemyOnField(nextField, figure.getColor())) {
                    possibleFields.add(nextField);
                    break;
                } else {
                    break;
                }
            }
        }

        return possibleFields;
    }

    //MOVE && ATTACK OPERATIONS

    public List<Figure> hitFigureFromBoard(Figure movingFigure, Field toField) {
        figures = figures.stream()
                .filter(figure -> !toField.equals(figure.getCurrentField()))
                .collect(Collectors.toList());
        return moveFigureToField(movingFigure, toField);
    }

    public List<Figure> moveFigureToField(Figure movingFigure, Field toField) {
        for(Figure figure : figures) {
            if(movingFigure.getCurrentPosition().equals(figure.getCurrentPosition())) {
                figure.setCurrentField(toField);
            }
        }
        return figures;
    }
}
