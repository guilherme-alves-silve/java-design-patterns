package com.iluwatar.asynccqrs.query;

import com.iluwatar.asynccqrs.dto.AuthorDTO;
import com.iluwatar.asynccqrs.dto.BookDTO;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface QueryService {

    CompletableFuture<Optional<AuthorDTO>> getAuthorByUsername(String username);

    CompletableFuture<Optional<BookDTO>> getBook(String title);

    CompletableFuture<List<BookDTO>> getAuthorBooks(String username);

    CompletableFuture<BigInteger> getAuthorBooksCount(String username);

    CompletableFuture<BigInteger> getAuthorsCount();

}
