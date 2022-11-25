package com.epam.engx.cleancode.finaltask.task1;

import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.Command;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DataSet;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.View;
import com.epam.engx.cleancode.finaltask.task1.thirdpartyjar.DatabaseManager;
import java.util.List;

public class Print implements Command {

    private static final String EMPTY_TABLE_TEXT_TEMPLATE = "║ Table '%s' is empty or does not exist ║";
    private static final String LEFT_TOP_CORNER_ELEMENT = "╔";
    private static final String BORDER_LINE_ELEMENT = "═";
    private static final String NEW_LINE = "\n";
    private static final String RIGHT_TOP_CORNER_ELEMENT = "╗";
    private static final String LEFT_BOTTOM_CORNER_ELEMENT = "╚";
    private static final String RIGHT_BOTTOM_CORNER_ELEMENT = "╝";
    private static final String VERTICAL_BORDER_ELEMENT = "║";
    private static final String LEFT_BORDER_CROSS_ELEMENT = "╠";
    private static final String MIDDLE_BORDER_CROSS_ELEMENT = "╬";
    private static final String RIGHT_BORDER_CROSS_ELEMENT = "╣";
    private static final String BOTTOM_BORDER_CROSS_ELEMENT = "╩";
    private static final String TOP_BORDER_CROSS_ELEMENT = "╦";
    private static final String TABLE_CONTENT_ELEMENT_SPACE = " ";
    private static final String PROCESS_VALIDATION_COMMAND = "print ";
    private static final String ILLEGAL_ARGUMENT_ERROR_MESSAGE_TEMPLATE = "incorrect number of parameters. Expected 1, but is  %s";
    private static final int NUMBER_OF_ENCLOSING_BORDER_ELEMENTS = 2;
    private static final int NUMBER_TO_ADD_IF_EVEN = 2;
    private static final int NUMBER_TO_ADD_IF_ODD = 3;
    private static final int NUMBER_TO_ADD_IF_BODY_LENGTH_ODD = 1;
    private static final int ALLOWED_COMMAND_LENGTH = 2;
    private static final int TABLE_NAME_POSITION = 1;

    private final View view;
    private final DatabaseManager manager;
    private String tableName;

    public Print(View view, DatabaseManager manager) {
        this.view = view;
        this.manager = manager;
    }

    public boolean canProcess(String command) {
        return command.startsWith(PROCESS_VALIDATION_COMMAND);
    }

    public void process(String input) {
        String[] command = input.split(" ");

        if (command.length != ALLOWED_COMMAND_LENGTH) {
            throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_ERROR_MESSAGE_TEMPLATE, (command.length - 1)));
        }

        tableName = command[TABLE_NAME_POSITION];
        List<DataSet> data = manager.getTableData(tableName);
        view.write(getTableString(data));
    }

    private String getTableString(List<DataSet> data) {
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
        longestLine = getLongestString(longestLine, columnNames);

        return longestLine.length();
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

        StringBuilder result = new StringBuilder(LEFT_TOP_CORNER_ELEMENT);
        result.append(buildLineFromElement(contentLength, BORDER_LINE_ELEMENT));
        result.append(RIGHT_TOP_CORNER_ELEMENT);
        result.append(NEW_LINE);
        result.append(tableText);
        result.append(NEW_LINE);
        result.append(LEFT_BOTTOM_CORNER_ELEMENT);
        result.append(buildLineFromElement(contentLength, BORDER_LINE_ELEMENT));
        result.append(RIGHT_BOTTOM_CORNER_ELEMENT);
        result.append(NEW_LINE);

        return result;
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
        result.append(buildTopTableLine(lengthOfTheLongestColumn, columnCount));

        for (int column = 0; column < columnCount; column++) {
            result.append(buildHeaderContent(lengthOfTheLongestColumn, columnNames, column));
        }

        result.append(VERTICAL_BORDER_ELEMENT).append(NEW_LINE);
        result.append(buildMiddleTableLine(columnCount, lengthOfTheLongestColumn));

        return result;
    }

    private int calculateLengthOfTheLongestColumn(List<DataSet> data) {
        int lengthOfTheLongestColumn = getLengthOfTheLongestColumn(data);

        if (isEvenNumberOfCharacters(lengthOfTheLongestColumn)) {
            lengthOfTheLongestColumn += NUMBER_TO_ADD_IF_EVEN;
        } else {
            lengthOfTheLongestColumn += NUMBER_TO_ADD_IF_ODD;
        }
        return lengthOfTheLongestColumn;
    }

    private boolean isEvenNumberOfCharacters(int columnLength) {
        return columnLength % 2 == 0;
    }

    private int getColumnCount(List<DataSet> dataSets) {
        return dataSets.isEmpty() ? 0 : dataSets.get(0).getColumnNames().size();
    }

    private StringBuilder buildTopTableLine(int columnLength, int columnCount) {
        StringBuilder result = new StringBuilder(LEFT_TOP_CORNER_ELEMENT);

        for (int j = 1; j < columnCount; j++) {
            result.append(buildLineFromElement(columnLength, BORDER_LINE_ELEMENT));
            result.append(TOP_BORDER_CROSS_ELEMENT);
        }

        result.append(buildLineFromElement(columnLength, BORDER_LINE_ELEMENT));
        result.append(Print.RIGHT_TOP_CORNER_ELEMENT).append(NEW_LINE);

        return  result;
    }

    private StringBuilder buildHeaderContent(int lengthOfTheLongestColumn, List<String> columnNames, int column) {
        StringBuilder result = new StringBuilder(VERTICAL_BORDER_ELEMENT);

        int columnNamesLength = columnNames.get(column).length();
        int currentColumnLength = ((lengthOfTheLongestColumn - columnNamesLength) / 2);

        if (isEvenNumberOfCharacters(columnNamesLength)) {
            result.append(buildTableDataIfContentLengthIsEven(currentColumnLength, columnNames.get(column)));
        } else {
            result.append(buildTableDataIfContentLengthIsOdd(currentColumnLength, columnNames.get(column)));
        }
        return result;
    }

    private StringBuilder buildMiddleTableLine(int columnCount, int columnSize) {
        StringBuilder result = new StringBuilder(LEFT_BORDER_CROSS_ELEMENT);
        for (int j = 1; j < columnCount; j++) {
            result.append(buildLineFromElement(columnSize, BORDER_LINE_ELEMENT));
            result.append(MIDDLE_BORDER_CROSS_ELEMENT);
        }
        result.append(buildLineFromElement(columnSize, BORDER_LINE_ELEMENT));
        result.append(RIGHT_BORDER_CROSS_ELEMENT).append(NEW_LINE);

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
                result.append(buildMiddleTableLine(columnCount, lengthOfTheLongestColumn));
            }
        }
        result.append(buildBottomTableLine(lengthOfTheLongestColumn, columnCount));

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
                result.append(buildTableDataIfContentLengthIsEven(currentColumnLength, values.get(column))).append(VERTICAL_BORDER_ELEMENT);
            } else {
                result.append(buildTableDataIfContentLengthIsOdd(currentColumnLength, values.get(column))).append(VERTICAL_BORDER_ELEMENT);
            }
        }
        return result.append(NEW_LINE);
    }

    private StringBuilder buildTableDataIfContentLengthIsEven(int columnLength, Object value) {
        StringBuilder result = new StringBuilder();
        result.append(buildLineFromElement(columnLength, TABLE_CONTENT_ELEMENT_SPACE));
        result.append(value);
        result.append(buildLineFromElement(columnLength, TABLE_CONTENT_ELEMENT_SPACE));

        return result;
    }

    private StringBuilder buildTableDataIfContentLengthIsOdd(int columnLength, Object value) {
        StringBuilder result = new StringBuilder();
        result.append(buildLineFromElement(columnLength, TABLE_CONTENT_ELEMENT_SPACE));
        result.append(value);
        result.append(buildLineFromElement(columnLength + NUMBER_TO_ADD_IF_BODY_LENGTH_ODD, TABLE_CONTENT_ELEMENT_SPACE));

        return result;
    }

    private StringBuilder buildBottomTableLine(int columnLength, int columnCount) {
        StringBuilder result = new StringBuilder(LEFT_BOTTOM_CORNER_ELEMENT);

        for (int j = 1; j < columnCount; j++) {
            result.append(buildLineFromElement(columnLength, BORDER_LINE_ELEMENT));
            result.append(BOTTOM_BORDER_CROSS_ELEMENT);
        }

        result.append(buildLineFromElement(columnLength, BORDER_LINE_ELEMENT));
        result.append(RIGHT_BOTTOM_CORNER_ELEMENT).append(NEW_LINE);
        return result;
    }

    private StringBuilder buildLineFromElement(int lineLength, String element) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lineLength; i++) {
            result.append(element);
        }
        return result;
    }
}
