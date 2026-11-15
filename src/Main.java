import java.util.Scanner;

import storage.Database;
import storage.FileStorage;
import query.QueryExecutor;

/**
 * Главный класс программы GamesDBMS.
 */
public class Main {

    public static void main(String[] args) {

        // Загружаем базу данных
        Database database = FileStorage.load();

        // Создаём исполнитель SQL-запросов
        QueryExecutor executor = new QueryExecutor(database);

        Scanner scanner = new Scanner(System.in);

        System.out.println("================================");
        System.out.println("       Welcome to GamesDBMS");
        System.out.println("================================");
        System.out.println("Enter SQL queries to work with database.");
        System.out.println("Type EXIT to close the program.");
        System.out.println();

        while (true) {

            System.out.print("GamesDBMS> ");

            String sql = scanner.nextLine().trim();

            // Проверяем команду выхода
            if (sql.equalsIgnoreCase("EXIT")) {
                FileStorage.save(database);

                System.out.println("Database saved.");
                System.out.println("Goodbye, my fellow game lover!");

                break;
            }

            // Пропускаем пустые строки
            if (sql.isEmpty()) {
                continue;
            }

            // Выполняем запрос
            try {
                executor.execute(sql);

            } catch (Exception e) {
                System.out.println(
                    "Error: " + e.getMessage()
                );
            }

            System.out.println();
        }

        scanner.close();
    }
}
