package com.iluwatar.mycqrs.query;

import com.iluwatar.mycqrs.domain.model.Author;
import com.iluwatar.mycqrs.domain.model.Book;
import com.iluwatar.mycqrs.dto.AuthorDTO;
import com.iluwatar.mycqrs.dto.BookDTO;
import com.iluwatar.mycqrs.util.DBTable;
import com.iluwatar.mycqrs.util.MemoryDatabase;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class QueryServiceImpl implements QueryService {

    private final MemoryDatabase db;

    public QueryServiceImpl() {
        this.db = MemoryDatabase.db();
    }

    @Override
    public Optional<AuthorDTO> getAuthorByUsername(final String username) {
        return findAuthorByUsername(username);
    }

    @Override
    public Optional<BookDTO> getBook(final String title) {
        return findBookByTitle(title);
    }

    @Override
    public List<BookDTO> getAuthorBooks(final String username) {
        return findAuthorsBookByUsername(username)
                .map(this::toDTO)
                .collect(toList());
    }

    @Override
    public BigInteger getAuthorBooksCount(final String username) {
        final var count = findAuthorsBookByUsername(username)
                .count();
        return BigInteger.valueOf(count);
    }

    @Override
    public BigInteger getAuthorsCount() {
        final var count = db.<Author>getList(DBTable.AUTHOR_TBL)
                .count();
        return BigInteger.valueOf(count);
    }

    private Stream<Book> findAuthorsBookByUsername(final String username) {
        return db.<Book>getList(DBTable.BOOK_TBL)
                .filter(book -> book.getAuthor().getUsername().equals(username));
    }

    private Optional<AuthorDTO> findAuthorByUsername(final String username) {

        return db.<Author>getList(DBTable.AUTHOR_TBL)
                .filter(author -> author.getUsername().equals(username))
                .map(this::toDTO)
                .findFirst();
    }

    private Optional<BookDTO> findBookByTitle(final String title) {

        return db.<Book>getList(DBTable.BOOK_TBL)
                .filter(book -> book.getTitle().equals(title))
                .map(this::toDTO)
                .findFirst();
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
}
