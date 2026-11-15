package query;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбирает SQL-подобные запросы.
 * Поддерживает INSERT, SELECT, UPDATE и DELETE.
 */
public class SQLParser {

    public enum QueryType {
        INSERT, SELECT, UPDATE, DELETE
    }

    /**
     * Структура разобранного запроса.
     */
    public static class ParsedQuery {

        public QueryType type;
        public String table;

        public List<String> columns = new ArrayList<>();
        public List<String> values = new ArrayList<>();

        public Map<String, String> assignments =
                new LinkedHashMap<>();

        public String conditionColumn;
        public String conditionOperator;
        public String conditionValue;
    }

    /**
     * Разбирает SQL-запрос.
     */
    public static ParsedQuery parse(String sql) {

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty SQL query!");
        }

        sql = sql.trim();

        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        if (sql.toUpperCase().startsWith("INSERT")) {
            return parseInsert(sql);
        }

        if (sql.toUpperCase().startsWith("SELECT")) {
            return parseSelect(sql);
        }

        if (sql.toUpperCase().startsWith("UPDATE")) {
            return parseUpdate(sql);
        }

        if (sql.toUpperCase().startsWith("DELETE")) {
            return parseDelete(sql);
        }

        throw new IllegalArgumentException(
                "Unsupported SQL command!"
        );
    }

    /**
     * Разбирает INSERT INTO.
     */
    private static ParsedQuery parseInsert(String sql) {

        Pattern pattern = Pattern.compile(
            "(?i)INSERT\\s+INTO\\s+(\\w+)" +
            "\\s*(?:\\(([^)]*)\\))?\\s*" +
            "VALUES\\s*\\((.*)\\)"
        );

        Matcher matcher = pattern.matcher(sql);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid INSERT query!"
            );
        }

        ParsedQuery query = new ParsedQuery();

        query.type = QueryType.INSERT;
        query.table = matcher.group(1);

        if (matcher.group(2) != null) {
            query.columns = splitValues(matcher.group(2));
        }

        query.values = splitValues(matcher.group(3));

        if (!query.columns.isEmpty()
                && query.columns.size() != query.values.size()) {
            throw new IllegalArgumentException(
                    "Columns and values count do not match!"
            );
        }

        return query;
    }

    /**
     * Разбирает SELECT.
     */
    private static ParsedQuery parseSelect(String sql) {

        Pattern pattern = Pattern.compile(
            "(?i)SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)" +
            "(?:\\s+WHERE\\s+(.+))?"
        );

        Matcher matcher = pattern.matcher(sql);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid SELECT query!"
            );
        }

        ParsedQuery query = new ParsedQuery();

        query.type = QueryType.SELECT;
        query.table = matcher.group(2);

        query.columns = splitValues(matcher.group(1));

        if (matcher.group(3) != null) {
            parseCondition(matcher.group(3), query);
        }

        return query;
    }

    /**
     * Разбирает UPDATE.
     */
    private static ParsedQuery parseUpdate(String sql) {

        Pattern pattern = Pattern.compile(
            "(?i)UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)" +
            "(?:\\s+WHERE\\s+(.+))?"
        );

        Matcher matcher = pattern.matcher(sql);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid UPDATE query!"
            );
        }

        ParsedQuery query = new ParsedQuery();

        query.type = QueryType.UPDATE;
        query.table = matcher.group(1);

        List<String> assignments = splitValues(matcher.group(2));

        for (String assignment : assignments) {

            String[] parts = assignment.split("=", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid UPDATE assignment!"
                );
            }

            String column = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            if (query.assignments.put(column, value) != null) {
                throw new IllegalArgumentException(
                        "Duplicate column in UPDATE!"
                );
            }
        }

        if (matcher.group(3) != null) {
            parseCondition(matcher.group(3), query);
        }

        return query;
    }

    /**
     * Разбирает DELETE.
     */
    private static ParsedQuery parseDelete(String sql) {

        Pattern pattern = Pattern.compile(
            "(?i)DELETE\\s+FROM\\s+(\\w+)" +
            "(?:\\s+WHERE\\s+(.+))?"
        );

        Matcher matcher = pattern.matcher(sql);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid DELETE query!"
            );
        }

        ParsedQuery query = new ParsedQuery();

        query.type = QueryType.DELETE;
        query.table = matcher.group(1);

        if (matcher.group(2) != null) {
            parseCondition(matcher.group(2), query);
        }

        return query;
    }

    /**
     * Разбирает условие WHERE.
     */
    private static void parseCondition(
            String condition, ParsedQuery query) {

        Pattern pattern = Pattern.compile(
            "(?i)^\\s*(\\w+)\\s*(>=|<=|<>|!=|=|>|<)\\s*(.+?)\\s*$"
        );

        Matcher matcher = pattern.matcher(condition);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid WHERE condition!"
            );
        }

        query.conditionColumn =
                matcher.group(1).trim().toLowerCase();

        query.conditionOperator = matcher.group(2);

        query.conditionValue = matcher.group(3).trim();
    }

    /**
     * Разделяет значения с учётом кавычек.
     * Запятая внутри строки не считается разделителем.
     */
    private static List<String> splitValues(String text) {

        List<String> result = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        char quote = '\0';

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if ((c == '\'' || c == '"')) {

                if (!insideQuotes) {
                    insideQuotes = true;
                    quote = c;
                } else if (c == quote) {
                    insideQuotes = false;
                }

                current.append(c);

            } else if (c == ',' && !insideQuotes) {

                result.add(current.toString().trim());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quotation mark!"
            );
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }
}
