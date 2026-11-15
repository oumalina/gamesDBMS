package storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * Отвечает за сохранение и загрузку базы данных.
 */
public class FileStorage {

    private static final String FILE_PATH = "data/database.dat";

    /**
     * Сохраняет базу данных в файл.
     */
    public static void save(Database database) {
        File file = new File(FILE_PATH);

        try {
            file.getParentFile().mkdirs();

            try (ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(file))) {

                out.writeObject(database);
            }

            System.out.println("Database saved successfully!");

        } catch (IOException e) {
            System.out.println("Error saving database: " + e.getMessage());
        }
    }

    /**
     * Загружает базу данных из файла.
     */
    public static Database load() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("New database created.");
            return new Database();
        }

        try (ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(file))) {

            Database database = (Database) in.readObject();

            System.out.println("Database loaded successfully!");
            return database;

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database: " + e.getMessage());
            return new Database();
        }
    }
}