package com.iluwatar.mycqrs.query;

import com.iluwatar.mycqrs.dto.AuthorDTO;
import com.iluwatar.mycqrs.dto.BookDTO;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface QueryService {

    Optional<AuthorDTO> getAuthorByUsername(String username);

    Optional<BookDTO> getBook(String title);

    List<BookDTO> getAuthorBooks(String username);

    BigInteger getAuthorBooksCount(String username);

    BigInteger getAuthorsCount();

}
