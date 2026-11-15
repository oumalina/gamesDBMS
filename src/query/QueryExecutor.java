package query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Game;
import model.Review;
import repository.GameRepository;
import repository.ReviewRepository;
import storage.Database;
import storage.FileStorage;

/**
 * Выполняет SQL-подобные запросы к базе данных GamesDBMS.
 */
public class QueryExecutor {

    private final Database database;
    private final GameRepository gameRepository;
    private final ReviewRepository reviewRepository;

    public QueryExecutor(Database database) {
        this.database = database;
        this.gameRepository = new GameRepository(database);
        this.reviewRepository = new ReviewRepository(database);
    }

    /**
     * Выполняет SQL-запрос.
     */
    public void execute(String sql) {

        SQLParser.ParsedQuery query = SQLParser.parse(sql);

        switch (query.type) {
            case INSERT:
                executeInsert(query);
                break;
            case SELECT:
                executeSelect(query);
                break;
            case UPDATE:
                executeUpdate(query);
                break;
            case DELETE:
                executeDelete(query);
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported query type!"
                );
        }
    }

    /**
     * Выполняет INSERT.
     */
    private void executeInsert(SQLParser.ParsedQuery query) {

        String table = query.table.toLowerCase();

        if (table.equals("games")) {

            List<String> columns = query.columns.isEmpty()
                    ? defaultGameColumns()
                    : query.columns;

            Map<String, String> values =
                    createValuesMap(columns, query.values);

            Game game = new Game(
                getInt(values, "id", 0),
                getString(values, "title", null),
                getString(values, "genre", null),
                getString(values, "developer", null),
                getDate(values, "releasedate"),
                getDouble(values, "price", 0.0),
                getBoolean(values, "multiplayer", false)
            );

            gameRepository.add(game);

        } else if (table.equals("reviews")) {

            List<String> columns = query.columns.isEmpty()
                    ? defaultReviewColumns()
                    : query.columns;

            Map<String, String> values =
                    createValuesMap(columns, query.values);

            Review review = new Review(
                getInt(values, "id", 0),
                getInt(values, "gameid", 0),
                getString(values, "username", null),
                getInt(values, "rating", 0),
                getString(values, "comment", null),
                getDate(values, "reviewdate"),
                getBoolean(values, "recommended", false)
            );

            reviewRepository.add(review);

        } else {
            throw new IllegalArgumentException(
                "Unknown table: " + table
            );
        }

        FileStorage.save(database);
        System.out.println("Record inserted successfully!");
    }


    /**
     * Выполняет SELECT.
     */
    private void executeSelect(SQLParser.ParsedQuery query) {

        String table = query.table.toLowerCase();

        if (table.equals("games")) {

            List<Game> result = new ArrayList<>();

            for (Game game : gameRepository.getAll()) {
                if (matches(game, query)) {
                    result.add(game);
                }
            }

            printGames(result, query.columns);

        } else if (table.equals("reviews")) {

            List<Review> result = new ArrayList<>();

            for (Review review : reviewRepository.getAll()) {
                if (matches(review, query)) {
                    result.add(review);
                }
            }

            printReviews(result, query.columns);

        } else {
            throw new IllegalArgumentException(
                "Unknown table: " + table
            );
        }
    }

    /**
     * Выполняет UPDATE.
     */
    private void executeUpdate(SQLParser.ParsedQuery query) {

        String table = query.table.toLowerCase();

        int count;

        if (table.equals("games")) {

            count = gameRepository.updateWhere(
                game -> matches(game, query),
                game -> updateGame(game, query.assignments)
            );

        } else if (table.equals("reviews")) {

            count = reviewRepository.updateWhere(
                review -> matches(review, query),
                review -> updateReview(review, query.assignments)
            );

        } else {
            throw new IllegalArgumentException(
                "Unknown table: " + table
            );
        }

        if (count > 0) {
            FileStorage.save(database);
        }

        System.out.println("Updated records: " + count);
    }

    /**
     * Выполняет DELETE.
     */
    private void executeDelete(SQLParser.ParsedQuery query) {

        String table = query.table.toLowerCase();

        int count;

        if (table.equals("games")) {

            count = gameRepository.deleteWhere(
                game -> matches(game, query)
            );

        } else if (table.equals("reviews")) {

            count = reviewRepository.deleteWhere(
                review -> matches(review, query)
            );

        } else {
            throw new IllegalArgumentException(
                "Unknown table: " + table
            );
        }

        if (count > 0) {
            FileStorage.save(database);
        }

        System.out.println("Deleted records: " + count);
    }

    /**
     * Проверяет условие WHERE для игры.
     */
    private boolean matches(
            Game game, SQLParser.ParsedQuery query) {

        if (query.conditionColumn == null) {
            return true;
        }

        Object value = getGameField(
                game, query.conditionColumn
        );

        return compare(value, query.conditionValue,
                query.conditionOperator);
    }

    /**
     * Проверяет условие WHERE для отзыва.
     */
    private boolean matches(
            Review review, SQLParser.ParsedQuery query) {

        if (query.conditionColumn == null) {
            return true;
        }

        Object value = getReviewField(
                review, query.conditionColumn
        );

        return compare(value, query.conditionValue,
                query.conditionOperator);
    }


    /**
     * Получает значение поля игры.
     */
    private Object getGameField(Game game, String column) {

        switch (column.toLowerCase()) {
            case "id": return game.getId();
            case "title": return game.getTitle();
            case "genre": return game.getGenre();
            case "developer": return game.getDeveloper();
            case "releasedate": return game.getReleaseDate();
            case "price": return game.getPrice();
            case "multiplayer": return game.isMultiplayer();

            default:
                throw new IllegalArgumentException(
                    "Unknown games column: " + column
                );
        }
    }

    /**
     * Получает значение поля отзыва.
     */
    private Object getReviewField(
            Review review, String column) {

        switch (column.toLowerCase()) {
            case "id": return review.getId();
            case "gameid": return review.getGameId();
            case "username": return review.getUsername();
            case "rating": return review.getRating();
            case "comment": return review.getComment();
            case "reviewdate": return review.getReviewDate();
            case "recommended": return review.isRecommended();

            default:
                throw new IllegalArgumentException(
                    "Unknown reviews column: " + column
                );
        }
    }

    /**
     * Сравнивает значение поля со значением из WHERE.
     */
    private boolean compare(
            Object actual, String expected, String operator) {

        String value = cleanValue(expected);

        if (actual == null) {
            return ("=".equals(operator)
                    && value.equalsIgnoreCase("NULL"))
                    || (("!=".equals(operator)
                    || "<>".equals(operator))
                    && !value.equalsIgnoreCase("NULL"));
        }

        int comparison;

        if (actual instanceof Number) {
            comparison = Double.compare(
                ((Number) actual).doubleValue(),
                Double.parseDouble(value)
            );

        } else if (actual instanceof LocalDate) {
            comparison = ((LocalDate) actual).compareTo(
                LocalDate.parse(value)
            );

        } else if (actual instanceof Boolean) {
            comparison = Boolean.compare(
                (Boolean) actual,
                Boolean.parseBoolean(value)
            );

        } else {
            comparison = actual.toString()
                    .compareToIgnoreCase(value);
        }

        switch (operator) {
            case "=": return comparison == 0;
            case "!=":
            case "<>": return comparison != 0;
            case ">": return comparison > 0;
            case "<": return comparison < 0;
            case ">=": return comparison >= 0;
            case "<=": return comparison <= 0;

            default:
                throw new IllegalArgumentException(
                    "Unknown comparison operator!"
                );
        }
    }

    /**
     * Обновляет поля игры.
     */
    private void updateGame(
            Game game, Map<String, String> assignments) {

        for (Map.Entry<String, String> entry
                : assignments.entrySet()) {

            String column = entry.getKey().toLowerCase();
            String value = cleanValue(entry.getValue());

            switch (column) {
                case "title":
                    game.setTitle(value);
                    break;
                case "genre":
                    game.setGenre(value);
                    break;
                case "developer":
                    game.setDeveloper(value);
                    break;
                case "releasedate":
                    game.setReleaseDate(LocalDate.parse(value));
                    break;
                case "price":
                    game.setPrice(Double.parseDouble(value));
                    break;
                case "multiplayer":
                    game.setMultiplayer(Boolean.parseBoolean(value));
                    break;
                case "id":
                    throw new IllegalArgumentException(
                        "Updating primary key is not allowed!"
                    );
                default:
                    throw new IllegalArgumentException(
                        "Unknown games column: " + column
                    );
            }
        }
    }

    /**
     * Обновляет поля отзыва.
     */
    private void updateReview(
            Review review, Map<String, String> assignments) {

        for (Map.Entry<String, String> entry
                : assignments.entrySet()) {

            String column = entry.getKey().toLowerCase();
            String value = cleanValue(entry.getValue());

            switch (column) {
                case "username":
                    review.setUsername(value);
                    break;
                case "rating":
                    review.setRating(Integer.parseInt(value));
                    break;
                case "comment":
                    review.setComment(value);
                    break;
                case "reviewdate":
                    review.setReviewDate(LocalDate.parse(value));
                    break;
                case "recommended":
                    review.setRecommended(Boolean.parseBoolean(value));
                    break;
                case "gameid":
                    review.setGameId(Integer.parseInt(value));
                    break;
                case "id":
                    throw new IllegalArgumentException(
                        "Updating primary key is not allowed!"
                    );
                default:
                    throw new IllegalArgumentException(
                        "Unknown reviews column: " + column
                    );
            }
        }
    }


    /**
     * Выводит игры в табличном виде.
     */
    private void printGames(
            List<Game> games, List<String> columns) {

        System.out.println("\nGAMES");

        if (columns.size() == 1 && columns.get(0).equals("*")) {
            System.out.printf(
                "%-5s %-22s %-16s %-18s %-12s %-10s %-12s%n",
                "ID", "TITLE", "GENRE", "DEVELOPER",
                "RELEASE DATE", "PRICE", "MULTIPLAYER"
            );

            for (Game game : games) {
                System.out.println(game);
            }

        } else {
            printProjectedRows(games, columns, true);
        }

        System.out.println("Total records: " + games.size());
    }

    /**
     * Выводит отзывы в табличном виде.
     */
    private void printReviews(
            List<Review> reviews, List<String> columns) {

        System.out.println("\nREVIEWS");

        if (columns.size() == 1 && columns.get(0).equals("*")) {
            System.out.printf(
                "%-5s %-8s %-15s %-8s %-25s %-12s %-12s%n",
                "ID", "GAME ID", "USERNAME", "RATING",
                "COMMENT", "DATE", "RECOMMENDED"
            );

            for (Review review : reviews) {
                System.out.println(review);
            }

        } else {
            printProjectedRows(reviews, columns, false);
        }

        System.out.println("Total records: " + reviews.size());
    }

    /**
     * Выводит выбранные столбцы.
     */
    private void printProjectedRows(
            List<?> records, List<String> columns,
            boolean isGame) {

        for (String column : columns) {
            System.out.printf("%-22s", column);
        }

        System.out.println();

        for (Object record : records) {

            for (String column : columns) {

                Object value = isGame
                        ? getGameField((Game) record, column)
                        : getReviewField((Review) record, column);

                System.out.printf("%-22s",
                        value == null ? "NULL" : value);
            }

            System.out.println();
        }
    }

    /**
     * Создаёт соответствие столбцов и значений.
     */
    private Map<String, String> createValuesMap(
            List<String> columns, List<String> values) {

        if (columns.size() != values.size()) {
            throw new IllegalArgumentException(
                "Columns and values count do not match!"
            );
        }

        Map<String, String> result =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < columns.size(); i++) {

            String column = columns.get(i).trim().toLowerCase();

            if (result.containsKey(column)) {
                throw new IllegalArgumentException(
                    "Duplicate column: " + column
                );
            }

            result.put(column, values.get(i));
        }

        return result;
    }

    private List<String> defaultGameColumns() {
        return java.util.Arrays.asList(
            "id", "title", "genre", "developer",
            "releasedate", "price", "multiplayer"
        );
    }

    private List<String> defaultReviewColumns() {
        return java.util.Arrays.asList(
            "id", "gameid", "username", "rating",
            "comment", "reviewdate", "recommended"
        );
    }

    private int getInt(
            Map<String, String> values, String key,
            int defaultValue) {

        return values.containsKey(key)
                ? Integer.parseInt(cleanValue(values.get(key)))
                : defaultValue;
    }

    private double getDouble(
            Map<String, String> values, String key,
            double defaultValue) {

        return values.containsKey(key)
                ? Double.parseDouble(cleanValue(values.get(key)))
                : defaultValue;
    }

    private String getString(
            Map<String, String> values, String key,
            String defaultValue) {

        return values.containsKey(key)
                ? cleanValue(values.get(key))
                : defaultValue;
    }

    private LocalDate getDate(
            Map<String, String> values, String key) {

        return values.containsKey(key)
                ? LocalDate.parse(cleanValue(values.get(key)))
                : null;
    }

    private boolean getBoolean(
            Map<String, String> values, String key,
            boolean defaultValue) {

        return values.containsKey(key)
                ? Boolean.parseBoolean(cleanValue(values.get(key)))
                : defaultValue;
    }

    /**
     * Убирает кавычки со строковых значений.
     */
    private String cleanValue(String value) {

        value = value.trim();

        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);

            if ((first == '\'' && last == '\'')
                    || (first == '"' && last == '"')) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}