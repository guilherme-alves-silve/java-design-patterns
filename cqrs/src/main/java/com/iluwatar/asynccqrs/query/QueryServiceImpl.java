package com.iluwatar.asynccqrs.query;

import com.iluwatar.asynccqrs.dto.AuthorDTO;
import com.iluwatar.asynccqrs.dto.BookDTO;
import com.iluwatar.asynccqrs.model.Author;
import com.iluwatar.asynccqrs.model.Book;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.iluwatar.asynccqrs.util.DatabaseUtils.executeFetch;
import static com.iluwatar.asynccqrs.util.DatabaseUtils.executeFetchAll;
import static com.iluwatar.asynccqrs.util.DatabaseUtils.executeFetchMono;

public class QueryServiceImpl implements QueryService {

    @Override
    public CompletableFuture<Optional<AuthorDTO>> getAuthorByUsername(final String username) {
        return findAuthorByUsername(username)
                .thenApply(optAuthor -> optAuthor.map(this::toDTO))
                .exceptionally(this::noSuchElementExceptionToOptional);
    }

    @Override
    public CompletableFuture<Optional<BookDTO>> getBook(final String title) {
        return findBookByTitle(title)
                .thenApply(optAuthor -> optAuthor.map(this::toDTO))
                .exceptionally(this::noSuchElementExceptionToOptional);
    }

    private <T> Optional<T> noSuchElementExceptionToOptional(Throwable throwable) {
        if ((throwable instanceof NoSuchElementException) ||
                throwable.getCause() instanceof NoSuchElementException) {
            return Optional.empty();
        }

        throw new RuntimeException(throwable);
    }

    @Override
    public CompletableFuture<List<BookDTO>> getAuthorBooks(final String username) {
        final var sql = "SELECT " +
                "b.title, " +
                "b.price " +
                "FROM book_tbl b " +
                "INNER JOIN author_tbl a " +
                "ON a.id = b.author_id " +
                "WHERE a.username = ?;";
        return executeFetchAll(sql, Map.of(0, username), (row, meta) -> new BookDTO(
                        row.get("title", String.class),
                        row.get("price", BigDecimal.class)
                ));
    }

    @Override
    public CompletableFuture<BigInteger> getAuthorBooksCount(final String username) {
        final var sql = "SELECT COUNT(1) FROM author_tbl a " +
                "INNER JOIN book_tbl b " +
                "ON a.id = b.author_id " +
                "WHERE username = ?;";
        return executeFetch(sql, Map.of(0, username), (row, rowMetadata) -> row.get(0, BigInteger.class));
    }

    @Override
    public CompletableFuture<BigInteger> getAuthorsCount() {
        final var sql = "SELECT COUNT(*) FROM author_tbl;";
        return executeFetch(sql, Map.of(), (row, rowMetadata) -> row.get(0, Long.class))
                .thenApply(BigInteger::valueOf);
    }

    private BookDTO toDTO(final Book book) {
        return new BookDTO(
                book.getTitle(),
                book.getPrice()
        );
    }

    private AuthorDTO toDTO(final Author author) {
        return new AuthorDTO(
                author.getName(),
                author.getEmail(),
                author.getUsername()
        );
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
                "WHERE b.title = ?;";
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
