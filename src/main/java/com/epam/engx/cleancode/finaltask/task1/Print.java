package com.epam.engx.cleancode.finaltask.task1;

import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;

import java.util.List;

public class Print implements Command {

    private static final String EMPTY_TABLE_TEXT_TEMPLATE = "║ Table '%s' is empty or does not exist ║";
    private static final String BORDER_LINE_ELEMENT = "═";
    private static final String NEW_LINE = "\n";
    private static final String VERTICAL_BORDER_ELEMENT = "║";
    private static final String TABLE_CONTENT_ELEMENT_SPACE = " ";
    private static final String PROCESS_VALIDATION_COMMAND = "print ";
    private static final String ILLEGAL_ARGUMENT_ERROR_MESSAGE_TEMPLATE = "incorrect number of parameters. Expected 1, but is  %s";
    private static final String COMMAND_DELIMITER = " ";
    private static final int NUMBER_OF_ENCLOSING_BORDER_ELEMENTS = 2;
    private static final int NUMBER_TO_ADD_IF_EVEN = 2;
    private static final int NUMBER_TO_ADD_IF_ODD = 3;
    private static final int NUMBER_TO_ADD_IF_BODY_LENGTH_ODD = 1;
    private static final int ALLOWED_COMMAND_LENGTH = 2;
    private static final int TABLE_NAME_POSITION = 1;

    private final View view;
    private final DatabaseManager manager;

    public Print(View view, DatabaseManager manager) {
        this.view = view;
        this.manager = manager;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith(PROCESS_VALIDATION_COMMAND);
    }

    @Override
    public void process(String input) {
        String[] commandParts = input.split(COMMAND_DELIMITER);

        validateCommandLengthOrThrow(commandParts);

        String tableName = commandParts[TABLE_NAME_POSITION];
        List<DataSet> data = manager.getTableData(tableName);
        view.write(getTableString(tableName, data));
    }

    private void validateCommandLengthOrThrow(String[] commandParts) {
        if (commandParts.length != ALLOWED_COMMAND_LENGTH) {
            throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_ERROR_MESSAGE_TEMPLATE, (commandParts.length - 1)));
        }
    }

    private String getTableString(String tableName, List<DataSet> data) {
        return isEmptyTable(data) ?
                buildEmptyTableWithName(tableName).toString() : buildTable(data).toString();
    }

    private boolean isEmptyTable(List<DataSet> data) {
        int lengthOfTheLongestColumn = getLengthOfTheLongestColumn(data);
        return lengthOfTheLongestColumn == 0;
    }

    private int getLengthOfTheLongestColumn(List<DataSet> dataSets) {
        if (dataSets.isEmpty()) {
            return 0;
        }
        int maxColumnNameLength = findLengthOfTheLongestColumnName(dataSets);
        int maxContentLineLength = findLongestContentLineLength(dataSets);

        return Math.max(maxColumnNameLength, maxContentLineLength);
    }

    private int findLengthOfTheLongestColumnName(List<DataSet> dataSets) {
        String longestLine = "";
        List<String> columnNames = dataSets.get(0).getColumnNames();
        return getLongestString(longestLine, columnNames).length();
    }

    private String getLongestString(String maxLength, List<?> values) {
        for (Object value : values) {
            if (String.valueOf(value).length() > maxLength.length()) {
                maxLength = String.valueOf(value);
            }
        }
        return maxLength;
    }

    private int findLongestContentLineLength(List<DataSet> dataSets) {
        String longestLine = "";

        for (DataSet dataSet : dataSets) {
            longestLine = getLongestString(longestLine, dataSet.getValues());
        }
        return String.valueOf(longestLine).length();
    }

    private StringBuilder buildEmptyTableWithName(String tableName) {
        String tableText = String.format(EMPTY_TABLE_TEXT_TEMPLATE, tableName);
        int contentLength = tableText.length() - NUMBER_OF_ENCLOSING_BORDER_ELEMENTS;

        return new StringBuilder(LevelBoundary.UPPER.leftBoundary)
                .append(duplicateSymbol(BORDER_LINE_ELEMENT, contentLength))
                .append(LevelBoundary.UPPER.rightBoundary)
                .append(NEW_LINE)
                .append(tableText)
                .append(NEW_LINE)
                .append(LevelBoundary.BOTTOM.leftBoundary)
                .append(duplicateSymbol(BORDER_LINE_ELEMENT, contentLength))
                .append(LevelBoundary.BOTTOM.rightBoundary)
                .append(NEW_LINE);
    }

    private StringBuilder buildTable(List<DataSet> data) {
        return buildTableHeader(data)
                .append(buildTableBodyWithData(data));
    }

    private StringBuilder buildTableHeader(List<DataSet> dataSets) {
        int lengthOfTheLongestColumn = calculateLengthOfTheLongestColumn(dataSets);
        int columnCount = getColumnCount(dataSets);
        List<String> columnNames = dataSets.get(0).getColumnNames();

        StringBuilder result = new StringBuilder();
        result.append(buildTableLine(lengthOfTheLongestColumn, columnCount, LevelBoundary.UPPER));

        for (int column = 0; column < columnCount; column++) {
            result.append(buildHeaderContent(lengthOfTheLongestColumn, columnNames, column));
        }

        result.append(VERTICAL_BORDER_ELEMENT).append(NEW_LINE);
        result.append(buildTableLine(lengthOfTheLongestColumn, columnCount, LevelBoundary.MIDDLE));
        return result;
    }

    private int calculateLengthOfTheLongestColumn(List<DataSet> data) {
        int lengthOfTheLongestColumn = getLengthOfTheLongestColumn(data);

        return isEvenNumberOfCharacters(lengthOfTheLongestColumn) ?
                lengthOfTheLongestColumn + NUMBER_TO_ADD_IF_EVEN :
                lengthOfTheLongestColumn + NUMBER_TO_ADD_IF_ODD;
    }

    private boolean isEvenNumberOfCharacters(int columnLength) {
        return columnLength % 2 == 0;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return dataSets.isEmpty() ? 0 : dataSets.get(0).getColumnNames().size();
    }

    private StringBuilder buildHeaderContent(int lengthOfTheLongestColumn, List<String> columnNames, int column) {
        int columnNamesLength = columnNames.get(column).length();
        int currentColumnLength = ((lengthOfTheLongestColumn - columnNamesLength) / 2);

        StringBuilder result = new StringBuilder(VERTICAL_BORDER_ELEMENT);

        if (isEvenNumberOfCharacters(columnNamesLength)) {
            result.append(buildTableDataIfContentLengthIsEven(currentColumnLength, columnNames.get(column)));
        } else {
            result.append(buildTableDataIfContentLengthIsOdd(currentColumnLength, columnNames.get(column)));
        }
        return result;
    }

    private StringBuilder buildTableBodyWithData(List<DataSet> dataSets) {
        int lengthOfTheLongestColumn = calculateLengthOfTheLongestColumn(dataSets);
        int rowsCount = dataSets.size();
        int columnCount = getColumnCount(dataSets);

        StringBuilder result = new StringBuilder();

        for (int row = 0; row < rowsCount; row++) {
            List<Object> values = dataSets.get(row).getValues();
            result.append(buildBody(lengthOfTheLongestColumn, columnCount, values));

            if (isNotLastRow(rowsCount, row)) {
                result.append(buildTableLine(lengthOfTheLongestColumn, columnCount, LevelBoundary.MIDDLE));
            }
        }
        result.append(buildTableLine(lengthOfTheLongestColumn, columnCount, LevelBoundary.BOTTOM));

        return result;
    }

    private boolean isNotLastRow(int rowsCount, int row) {
        return row < rowsCount - 1;
    }

    private StringBuilder buildBody(int maxColumnLength, int columnCount, List<Object> values) {
        StringBuilder result = new StringBuilder(VERTICAL_BORDER_ELEMENT);

        for (int column = 0; column < columnCount; column++) {
            int currentCellContentLength = String.valueOf(values.get(column)).length();
            int currentColumnLength = (maxColumnLength - currentCellContentLength) / 2;

            if (isEvenNumberOfCharacters(currentCellContentLength)) {
                result.append(buildTableDataIfContentLengthIsEven(currentColumnLength, values.get(column)));
            } else {
                result.append(buildTableDataIfContentLengthIsOdd(currentColumnLength, values.get(column)));
            }
            result.append(VERTICAL_BORDER_ELEMENT);
        }
        return result.append(NEW_LINE);
    }

    private StringBuilder buildTableDataIfContentLengthIsEven(int columnLength, Object value) {
        return new StringBuilder()
                .append(duplicateSymbol(TABLE_CONTENT_ELEMENT_SPACE, columnLength))
                .append(value)
                .append(duplicateSymbol(TABLE_CONTENT_ELEMENT_SPACE, columnLength));
    }

    private StringBuilder buildTableDataIfContentLengthIsOdd(int columnLength, Object value) {
        return new StringBuilder()
                .append(duplicateSymbol(TABLE_CONTENT_ELEMENT_SPACE, columnLength))
                .append(value)
                .append(duplicateSymbol(TABLE_CONTENT_ELEMENT_SPACE, columnLength + NUMBER_TO_ADD_IF_BODY_LENGTH_ODD));
    }

    private StringBuilder buildTableLine(int columnLength, int columnCount, LevelBoundary elements) {

        String columnLineTemplate = duplicateSymbol(BORDER_LINE_ELEMENT, columnLength);
        String lineWithoutLastColumn = duplicateSymbol(columnLineTemplate + elements.middleBoundary, columnCount - 1);
        String lastColumnLine = columnLineTemplate + elements.rightBoundary;
        return new StringBuilder(elements.leftBoundary)
                .append(lineWithoutLastColumn)
                .append(lastColumnLine)
                .append(NEW_LINE);
    }

    private String duplicateSymbol(String symbol, int times) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < times; i++) {
            result.append(symbol);
        }
        return result.toString();
    }

    private enum LevelBoundary {
        UPPER("╔", "╦", "╗"),
        MIDDLE("╠", "╬", "╣"),
        BOTTOM("╚", "╩", "╝");

        private final String leftBoundary;
        private final String middleBoundary;
        private final String rightBoundary;

        LevelBoundary(String leftBoundary, String middleBoundary, String rightBoundary) {
            this.leftBoundary = leftBoundary;
            this.middleBoundary = middleBoundary;
            this.rightBoundary = rightBoundary;
        }
    }
}
