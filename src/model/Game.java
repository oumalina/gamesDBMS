
package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Модель видеоигры.
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String genre;
    private String developer;
    private LocalDate releaseDate;
    private double price;
    private boolean multiplayer;

    public Game(int id, String title, String genre,
                String developer, LocalDate releaseDate,
                double price, boolean multiplayer) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.developer = developer;
        this.releaseDate = releaseDate;
        this.price = price;
        this.multiplayer = multiplayer;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDeveloper() { return developer; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public double getPrice() { return price; }
    public boolean isMultiplayer() { return multiplayer; }

    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public void setPrice(double price) { this.price = price; }
    public void setMultiplayer(boolean multiplayer) { this.multiplayer = multiplayer; }

    @Override
    public String toString() {
        return String.format(
            "%-4d %-22s %-16s %-18s %-12s %-8.2f %-10s",
            id, title, genre, developer, releaseDate, price, multiplayer
        );
    }
}