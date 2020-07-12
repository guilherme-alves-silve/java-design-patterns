package com.iluwatar.mycqrs.command;

public interface CommandService {

    void authorCreated(final String username, final String name, final String email);

    void bookAddedToAuthor(final String title, final double price, final String username);

    void authorNameUpdated(final String username, final String name);

    void authorUsernameUpdated(final String oldUsername, final String newUsername);

    void authorEmailUpdated(final String username, final String email);

    void bookTitleUpdated(final String oldTitle, final String newTitle);

    void bookPriceUpdated(final String title, final double price);

}
