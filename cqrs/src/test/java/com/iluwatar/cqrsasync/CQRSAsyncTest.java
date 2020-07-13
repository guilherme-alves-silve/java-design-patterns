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

import com.iluwatar.cqrsasync.command.CommandServiceImpl;
import com.iluwatar.cqrsasync.dto.AuthorDTO;
import com.iluwatar.cqrsasync.dto.BookDTO;
import com.iluwatar.cqrsasync.query.QueryService;
import com.iluwatar.cqrsasync.query.QueryServiceImpl;
import com.iluwatar.cqrsasync.util.DatabaseInit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of IQueryService and ICommandService with h2 data
 */
public class CQRSAsyncTest {

    private static QueryService queryService;

    @BeforeAll
    public static void initializeAndPopulateDatabase() {

        DatabaseInit.init();

        var commandService = new CommandServiceImpl();
        queryService = new QueryServiceImpl();

        // create first author1
        commandService.authorCreated("username1", "name1", "email1").join();

        // create author1 and update all its data
        commandService.authorCreated("username2", "name2", "email2").join();
        commandService.authorEmailUpdated("username2", "new_email2").join();
        commandService.authorNameUpdated("username2", "new_name2").join();
        commandService.authorUsernameUpdated("username2", "new_username2").join();

        // add book1 to author1
        commandService.bookAddedToAuthor("title1", BigDecimal.valueOf(10), "username1").join();

        // add book2 to author1 and update all its data
        commandService.bookAddedToAuthor("title2", BigDecimal.valueOf(20), "username1").join();
        commandService.bookPriceUpdated("title2", BigDecimal.valueOf(30)).join();
        commandService.bookTitleUpdated("title2", "new_title2").join();
    }

    @AfterAll
    public static void destroy() {
        DatabaseInit.destroy();
    }

    @Test
    public void shouldGetAuthorByUsername() {
        var optAuthor = queryService.getAuthorByUsername("username1").join();
        assertTrue(optAuthor.isPresent());

        var author = optAuthor.get();
        assertEquals("username1", author.getUsername());
        assertEquals("name1", author.getName());
        assertEquals("email1", author.getEmail());
    }

    @Test
    public void shouldGetUpdatedAuthorByUsername() {
        var optAuthor = queryService.getAuthorByUsername("new_username2").join();
        assertTrue(optAuthor.isPresent());

        var author = optAuthor.get();
        var expectedAuthor = new AuthorDTO("new_name2", "new_email2", "new_username2");
        assertEquals(expectedAuthor, author);

    }

    @Test
    public void shouldGetBook() {
        var optBook = queryService.getBook("title1").join();
        assertTrue(optBook.isPresent());

        var book = optBook.get();
        assertEquals("title1", book.getTitle());
        assertEquals(BigDecimal.valueOf(10).setScale(2, RoundingMode.HALF_DOWN), book.getPrice());
    }

    @Test
    public void shouldGetAuthorBooks() {
        var books = queryService.getAuthorBooks("username1").join();
        assertEquals(2, books.size());
        assertTrue(books.contains(new BookDTO("title1", BigDecimal.valueOf(10))));
        assertTrue(books.contains(new BookDTO("new_title2", BigDecimal.valueOf(30))));
    }

    @Test
    public void shouldGetAuthorBooksCount() {
        var bookCount = queryService.getAuthorBooksCount("username1").join();
        assertEquals(new BigInteger("2"), bookCount);
    }

    @Test
    public void shouldGetAuthorsCount() {
        var authorCount = queryService.getAuthorsCount().join();
        assertEquals(new BigInteger("2"), authorCount);
    }
}
