package com.iluwatar.asynccqrs.command;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface CommandService {

    CompletableFuture<Boolean> authorCreated(final String username, final String name, final String email);

    CompletableFuture<Boolean> bookAddedToAuthor(final String title, final BigDecimal price, final String username);

    CompletableFuture<Boolean> authorNameUpdated(final String username, final String name);

    CompletableFuture<Boolean> authorUsernameUpdated(final String oldUsername, final String newUsername);

    CompletableFuture<Boolean> authorEmailUpdated(final String username, final String email);

    CompletableFuture<Boolean> bookTitleUpdated(final String oldTitle, final String newTitle);

    CompletableFuture<Boolean> bookPriceUpdated(final String title, final BigDecimal price);

}
