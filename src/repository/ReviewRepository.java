package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import model.Review;
import storage.Database;

/**
 * Репозиторий для работы с таблицей Reviews.
 */
public class ReviewRepository {

    private final Database database;

    public ReviewRepository(Database database) {
        this.database = database;
    }

    /**
     * Добавляет отзыв с проверкой уникальности ID
     * и существования связанной игры.
     */
    public void add(Review review) {

        if (findById(review.getId()).isPresent()) {
            throw new IllegalArgumentException(
                "Review with this ID already exists!"
            );
        }

        boolean gameExists = database.getGames()
                .stream()
                .anyMatch(game -> game.getId() == review.getGameId());

        if (!gameExists) {
            throw new IllegalArgumentException(
                "Game with ID " + review.getGameId()
                + " does not exist!"
            );
        }

        database.getReviews().add(review);
    }

    /**
     * Находит отзыв по первичному ключу.
     */
    public Optional<Review> findById(int id) {
        return database.getReviews()
                .stream()
                .filter(review -> review.getId() == id)
                .findFirst();
    }

    /**
     * Возвращает все отзывы.
     */
    public List<Review> getAll() {
        return new ArrayList<>(database.getReviews());
    }

    /**
     * Возвращает отзывы конкретной игры.
     */
    public List<Review> findByGameId(int gameId) {
        List<Review> result = new ArrayList<>();

        for (Review review : database.getReviews()) {
            if (review.getGameId() == gameId) {
                result.add(review);
            }
        }

        return result;
    }

    /**
     * Обновляет отзывы, удовлетворяющие условию.
     */
    public int updateWhere(Predicate<Review> condition,
                           Consumer<Review> updater) {
        int count = 0;

        for (Review review : database.getReviews()) {
            if (condition.test(review)) {

                int oldGameId = review.getGameId();

                updater.accept(review);

                boolean gameExists = database.getGames()
                        .stream()
                        .anyMatch(game ->
                                game.getId() == review.getGameId());

                if (!gameExists) {
                    review.setGameId(oldGameId);

                    throw new IllegalArgumentException(
                        "The specified game does not exist!"
                    );
                }

                count++;
            }
        }

        return count;
    }

    /**
     * Удаляет отзывы, удовлетворяющие условию.
     */
    public int deleteWhere(Predicate<Review> condition) {

        List<Review> toDelete = new ArrayList<>();

        for (Review review : database.getReviews()) {
            if (condition.test(review)) {
                toDelete.add(review);
            }
        }

        database.getReviews().removeAll(toDelete);

        return toDelete.size();
    }
}