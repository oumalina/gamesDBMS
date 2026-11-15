package storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.Game;
import model.Review;

/**
 * Представляет базу данных GamesDBMS.
 * Содержит таблицы игр и отзывов.
 */
public class Database implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Game> games;
    private List<Review> reviews;

    public Database() {
        games = new ArrayList<>();
        reviews = new ArrayList<>();
    }

    public List<Game> getGames() {
        return games;
    }

    public List<Review> getReviews() {
        return reviews;
    }
}