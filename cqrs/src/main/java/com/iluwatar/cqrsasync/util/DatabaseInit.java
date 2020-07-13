package com.iluwatar.cqrsasync.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DatabaseInit {

    private static final int MAX_WAIT_TIME = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInit.class);

    private DatabaseInit() {
        throw new IllegalArgumentException("No DatabaseInit!");
    }

    public static void init() {

        try {

            Runtime.getRuntime()
                    .exec("docker run -p 3306:3306 --name library_cqrs_async -e MYSQL_USER=root -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=library -d mysql")
                    .waitFor(MAX_WAIT_TIME, TimeUnit.SECONDS);

            Runtime.getRuntime()
                    .exec("docker start library_cqrs_async")
                    .waitFor(MAX_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("Failed to execute docker: " + ex.getMessage());
        }

        final var connection = Database.connect()
                .block();

        final var dropAuthorTableSQL = "DROP TABLE IF EXISTS author_tbl;";

        final var dropBookTableSQL = "DROP TABLE IF EXISTS book_tbl;";

        final var createAuthorTableSQL = "CREATE TABLE author_tbl (" +
                "    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "    username VARCHAR(255) UNIQUE NOT NULL," +
                "    name VARCHAR(255) NOT NULL," +
                "    email VARCHAR(255) NOT NULL" +
                ");";

        final var createBookTableSQL = "CREATE TABLE book_tbl (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    title VARCHAR(255) NOT NULL," +
                "    price DECIMAL(10, 2) NOT NULL," +
                "    author_id INT NOT NULL," +
                "    FOREIGN KEY(author_id) REFERENCES author_tbl(id)" +
                ");";

        Stream.of(dropBookTableSQL, dropAuthorTableSQL, createAuthorTableSQL, createBookTableSQL)
                .forEach(sql -> Mono.from(connection.createStatement(sql)
                        .execute())
                        .block());

        connection.close()
                .block();
    }

    public static void destroy() {
        try {
            Runtime.getRuntime()
                    .exec("docker stop -t 0 library_cqrs_async && docker rm library_cqrs_async")
                    .waitFor(MAX_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("Failed to stop docker: " + ex.getMessage());
        }
    }
}
