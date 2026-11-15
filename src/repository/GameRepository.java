package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import model.Game;
import storage.Database;

/**
 * Репозиторий для работы с таблицей Games.
 * Реализует основные операции над записями.
 */
public class GameRepository {

    private final Database database;

    public GameRepository(Database database) {
        this.database = database;
    }

    /**
     * Добавляет игру с проверкой уникальности ID.
     */
    public void add(Game game) {
        if (findById(game.getId()).isPresent()) {
            throw new IllegalArgumentException(
                "Game with this ID already exists!"
            );
        }

        database.getGames().add(game);
    }

    /**
     * Находит игру по первичному ключу.
     */
    public Optional<Game> findById(int id) {
        return database.getGames()
                .stream()
                .filter(game -> game.getId() == id)
                .findFirst();
    }

    /**
     * Возвращает все игры.
     */
    public List<Game> getAll() {
        return new ArrayList<>(database.getGames());
    }

    /**
     * Обновляет записи, удовлетворяющие условию.
     */
    public int updateWhere(Predicate<Game> condition,
                           Consumer<Game> updater) {
        int count = 0;

        for (Game game : database.getGames()) {
            if (condition.test(game)) {
                updater.accept(game);
                count++;
            }
        }

        return count;
    }

    /**
     * Удаляет записи, удовлетворяющие условию.
     * Не позволяет удалить игру, если у неё есть отзывы.
     */
    public int deleteWhere(Predicate<Game> condition) {

        List<Game> toDelete = new ArrayList<>();

        for (Game game : database.getGames()) {
            if (condition.test(game)) {
                boolean hasReviews = database.getReviews()
                        .stream()
                        .anyMatch(review ->
                                review.getGameId() == game.getId());

                if (hasReviews) {
                    throw new IllegalStateException(
                        "Cannot delete game with existing reviews: "
                        + game.getTitle()
                    );
                }

                toDelete.add(game);
            }
        }

        database.getGames().removeAll(toDelete);

        return toDelete.size();
    }
}