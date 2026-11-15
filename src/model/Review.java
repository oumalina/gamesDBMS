
package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Модель пользовательского отзыва.
 */
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int gameId;
    private String username;
    private int rating;
    private String comment;
    private LocalDate reviewDate;
    private boolean recommended;

    public Review(int id, int gameId, String username,
                  int rating, String comment,
                  LocalDate reviewDate, boolean recommended) {
        this.id = id;
        this.gameId = gameId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.recommended = recommended;
    }

    public int getId() { return id; }
    public int getGameId() { return gameId; }
    public String getUsername() { return username; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDate getReviewDate() { return reviewDate; }
    public boolean isRecommended() { return recommended; }

    public void setUsername(String username) { this.username = username; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }

    @Override
    public String toString() {
        return String.format(
            "%-4d %-8d %-15s %-8d %-25s %-12s %-10s",
            id, gameId, username, rating, comment, reviewDate, recommended
        );
    }
}