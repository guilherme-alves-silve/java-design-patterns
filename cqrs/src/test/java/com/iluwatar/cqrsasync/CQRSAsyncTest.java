/*
 * The MIT License
 * Copyright © 2014-2019 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.cqrsasync;

import com.iluwatar.mycqrs.command.CommandServiceImpl;
import com.iluwatar.mycqrs.dto.AuthorDTO;
import com.iluwatar.mycqrs.dto.BookDTO;
import com.iluwatar.mycqrs.query.QueryService;
import com.iluwatar.mycqrs.query.QueryServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of IQueryService and ICommandService with h2 data
 */
public class CQRSAsyncTest {

    private static QueryService queryService;

    @BeforeAll
    public static void initializeAndPopulateDatabase() {
        var commandService = new CommandServiceImpl();
        queryService = new QueryServiceImpl();

        // create first author1
        commandService.authorCreated("username1", "name1", "email1");

        // create author1 and update all its data
        commandService.authorCreated("username2", "name2", "email2");
        commandService.authorEmailUpdated("username2", "new_email2");
        commandService.authorNameUpdated("username2", "new_name2");
        commandService.authorUsernameUpdated("username2", "new_username2");

        // add book1 to author1
        commandService.bookAddedToAuthor("title1", 10, "username1");

        // add book2 to author1 and update all its data
        commandService.bookAddedToAuthor("title2", 20, "username1");
        commandService.bookPriceUpdated("title2", 30);
        commandService.bookTitleUpdated("title2", "new_title2");
    }

    @Test
    public void shouldGetAuthorByUsername() {
        var optAuthor = queryService.getAuthorByUsername("username1");
        assertTrue(optAuthor.isPresent());

        var author = optAuthor.get();
        assertEquals("username1", author.getUsername());
        assertEquals("name1", author.getName());
        assertEquals("email1", author.getEmail());
    }

    @Test
    public void shouldGetUpdatedAuthorByUsername() {
        var optAuthor = queryService.getAuthorByUsername("new_username2");
        assertTrue(optAuthor.isPresent());

        var author = optAuthor.get();
        var expectedAuthor = new AuthorDTO("new_name2", "new_email2", "new_username2");
        assertEquals(expectedAuthor, author);

    }

    @Test
    public void shouldGetBook() {
        var optBook = queryService.getBook("title1");
        assertTrue(optBook.isPresent());

        var book = optBook.get();
        assertEquals("title1", book.getTitle());
        assertEquals(10, book.getPrice(), 0.01);
    }

    @Test
    public void shouldGetAuthorBooks() {
        var books = queryService.getAuthorBooks("username1");
        assertEquals(2, books.size());
        assertTrue(books.contains(new BookDTO("title1", 10)));
        assertTrue(books.contains(new BookDTO("new_title2", 30)));
    }

    @Test
    public void shouldGetAuthorBooksCount() {
        var bookCount = queryService.getAuthorBooksCount("username1");
        assertEquals(new BigInteger("2"), bookCount);
    }

    @Test
    public void shouldGetAuthorsCount() {
        var authorCount = queryService.getAuthorsCount();
        assertEquals(new BigInteger("2"), authorCount);
    }
}
