package com.iluwatar.mycqrs.command;

import com.iluwatar.mycqrs.domain.model.Author;
import com.iluwatar.mycqrs.domain.model.Book;
import com.iluwatar.mycqrs.util.DBTable;
import com.iluwatar.mycqrs.util.MemoryDatabase;

import java.util.Optional;

public class CommandServiceImpl implements CommandService {

    private final MemoryDatabase db;

    public CommandServiceImpl() {
        this.db = MemoryDatabase.db();
    }

    @Override
    public void authorCreated(final String username, final String name, final String email) {

        final var author = new Author(username, name, email);
        db.save(DBTable.AUTHOR_TBL, author);
    }

    @Override
    public void bookAddedToAuthor(final String title, final double price, final String username) {

        final var optAuthor = findAuthorByUsername(username);
        optAuthor.ifPresent(author -> {
            final var book = new Book(title, price, author);
            db.save(DBTable.BOOK_TBL, book);
        });
    }

    @Override
    public void authorNameUpdated(final String username, final String name) {
        final var optAuthor = findAuthorByUsername(username);
        optAuthor.ifPresent(author -> author.setName(name));
    }

    @Override
    public void authorUsernameUpdated(final String oldUsername, final String newUsername) {
        final var optAuthor = findAuthorByUsername(oldUsername);
        optAuthor.ifPresent(author -> author.setUsername(newUsername));
    }

    @Override
    public void authorEmailUpdated(final String username, final String email) {
        final var optAuthor = findAuthorByUsername(username);
        optAuthor.ifPresent(author -> author.setEmail(email));
    }

    @Override
    public void bookTitleUpdated(final String oldTitle, final String newTitle) {
        final var optBook = findBookByTitle(oldTitle);
        optBook.ifPresent(book -> book.setTitle(newTitle));
    }

    @Override
    public void bookPriceUpdated(final String title, final double price) {
        final var optBook = findBookByTitle(title);
        optBook.ifPresent(book -> book.setPrice(price));
    }

    private Optional<Author> findAuthorByUsername(final String username) {

        return db.<Author>getList(DBTable.AUTHOR_TBL)
            .filter(author -> author.getUsername().equals(username))
            .findFirst();
    }

    private Optional<Book> findBookByTitle(final String title) {

        return db.<Book>getList(DBTable.BOOK_TBL)
                .filter(book -> book.getTitle().equals(title))
                .findFirst();
    }
}
