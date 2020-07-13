package com.iluwatar.cqrsasync.command;

import com.iluwatar.cqrsasync.model.Author;
import com.iluwatar.cqrsasync.model.Book;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.iluwatar.cqrsasync.util.DatabaseUtils.executeFetchMono;
import static com.iluwatar.cqrsasync.util.DatabaseUtils.executeStatement;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class CommandServiceImpl implements CommandService {

    @Override
    public CompletableFuture<Boolean> authorCreated(final String username, final String name, final String email) {

        final var sql = "INSERT INTO author_tbl (username, name, email) VALUES (?, ?, ?);";
        return executeStatement(sql, Map.of(
                0, username,
                1, name,
                2, email
        ));
    }

    @Override
    public CompletableFuture<Boolean> bookAddedToAuthor(final String title, final BigDecimal price, final String username) {

        final var sql = "INSERT INTO book_tbl (title, price, author_id) VALUES (?, ?, ?);";

        return findAuthorByUsername(username)
                .thenCompose(optAuthor -> {
                    if (optAuthor.isEmpty()) {
                        return completedFuture(false);
                    }

                    final var author = optAuthor.get();
                    return executeStatement(sql, Map.of(
                            0, title,
                            1, price,
                            2, author.getId()
                    ));
                });
    }

    @Override
    public CompletableFuture<Boolean> authorNameUpdated(final String username, final String name) {

        final var sql = "UPDATE author_tbl SET name = ? WHERE username = ?;";
        return executeStatement(sql, Map.of(
                0, name,
                1, username
        ));
    }

    @Override
    public CompletableFuture<Boolean> authorUsernameUpdated(final String oldUsername, final String newUsername) {

        final var sql = "UPDATE author_tbl SET username = ? WHERE username = ?;";
        return executeStatement(sql, Map.of(
                0, newUsername,
                1, oldUsername
        ));
    }

    @Override
    public CompletableFuture<Boolean> authorEmailUpdated(final String username, final String email) {

        final var sql = "UPDATE author_tbl SET email = ? WHERE username = ?;";
        return executeStatement(sql, Map.of(
                0, email,
                1, username
        ));
    }

    @Override
    public CompletableFuture<Boolean> bookTitleUpdated(final String oldTitle, final String newTitle) {

        final var sql = "UPDATE book_tbl SET title = ? WHERE title = ?";

        return findBookByTitle(oldTitle)
                .thenCompose(optBook -> {
                    if (optBook.isEmpty()) {
                        return completedFuture(false);
                    }

                    return executeStatement(sql, Map.of(
                            0, newTitle,
                            1, oldTitle
                    ));
                });
    }

    @Override
    public CompletableFuture<Boolean> bookPriceUpdated(final String title, final BigDecimal price) {

        final var sql = "UPDATE book_tbl SET price = ? WHERE title = ?";

        return findBookByTitle(title)
                .thenCompose(optBook -> {
                    if (optBook.isEmpty()) {
                        return completedFuture(false);
                    }

                    return executeStatement(sql, Map.of(
                            0, price,
                            1, title
                    ));
                });
    }

    private CompletableFuture<Optional<Author>> findAuthorByUsername(final String username) {

        final var sql = "SELECT * FROM author_tbl WHERE username = ?;";
        return executeFetchMono(sql, Map.of(0, username), (row, meta) -> new Author(
                row.get("id", Integer.class),
                row.get("username", String.class),
                row.get("name", String.class),
                row.get("email", String.class)
        ))
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()))
        .toFuture();
    }

    private CompletableFuture<Optional<Book>> findBookByTitle(final String title) {

        final var sql = "SELECT " +
                "a.id author_id," +
                "a.username author_username," +
                "a.name author_name," +
                "a.email author_email," +
                "b.id," +
                "b.title," +
                "b.price " +
                "FROM book_tbl b " +
                "INNER JOIN author_tbl a " +
                "ON a.id = b.author_id " +
                "WHERE b.title = ?title;";
        return executeFetchMono(sql, Map.of(0, title), (row, meta) ->
                new Book(
                    row.get("id", Long.class),
                    row.get("title", String.class),
                    row.get("price", BigDecimal.class),
                    new Author(
                        row.get("author_id", Integer.class),
                        row.get("author_username", String.class),
                        row.get("author_name", String.class),
                        row.get("author_email", String.class)
                    )
                ))
                .map(Optional::of)
                .switchIfEmpty(Mono.just(Optional.empty()))
                .toFuture();
    }
}
