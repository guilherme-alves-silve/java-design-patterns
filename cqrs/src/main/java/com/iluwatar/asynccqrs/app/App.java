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

package com.iluwatar.asynccqrs.app;

import com.iluwatar.asynccqrs.command.CommandServiceImpl;
import com.iluwatar.asynccqrs.query.QueryServiceImpl;
import com.iluwatar.asynccqrs.util.DatabaseInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * CQRS : Command Query Responsibility Segregation. A pattern used to separate query services from
 * commands or writes services. The pattern is very simple but it has many consequences. For
 * example, it can be used to tackle down a complex domain, or to use other architectures that were
 * hard to implement with the classical way.
 *
 * <p>This implementation is an example of managing books and authors in a library. The persistence
 * of books and authors is done according to the CQRS architecture. A command side that deals with a
 * data model to persist(insert,update,delete) objects to a database. And a query side that uses
 * native queries to get data from the database and return objects as DTOs (Data transfer Objects).
 */
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static final String E_EVANS = "eEvans";
    public static final String J_BLOCH = "jBloch";
    public static final String M_FOWLER = "mFowler";

    /**
     * Program entry point.
     *
     * @param args command line args
     */
    public static void main(String[] args) {

        try {
            DatabaseInit.init();

            var commands = new CommandServiceImpl();

            // Create Authors and Books using CommandService
            commands.authorCreated(E_EVANS, "Eric Evans", "evans@email.com").join();
            commands.authorCreated(J_BLOCH, "Joshua Bloch", "jBloch@email.com").join();
            commands.authorCreated(M_FOWLER, "Martin Fowler", "mFowler@email.com").join();

            commands.bookAddedToAuthor("Domain-Driven Design", BigDecimal.valueOf(60.08), E_EVANS);
            commands.bookAddedToAuthor("Effective Java", BigDecimal.valueOf(40.54), J_BLOCH);
            commands.bookAddedToAuthor("Java Puzzlers", BigDecimal.valueOf(39.99), J_BLOCH);
            commands.bookAddedToAuthor("Java Concurrency in Practice", BigDecimal.valueOf(29.40), J_BLOCH);
            commands.bookAddedToAuthor("Patterns of Enterprise"
                    + " Application Architecture", BigDecimal.valueOf(54.01), M_FOWLER);
            commands.bookAddedToAuthor("Domain Specific Languages", BigDecimal.valueOf(48.89), M_FOWLER);
            commands.authorNameUpdated(E_EVANS, "Eric J. Evans");

            var queries = new QueryServiceImpl();

            // Query the database using QueryService
            var optEmptyAuthor = queries.getAuthorByUsername("username").join();
            var optEvans = queries.getAuthorByUsername(E_EVANS).join();
            var blochBooksCount = queries.getAuthorBooksCount(J_BLOCH).join();
            var authorsCount = queries.getAuthorsCount().join();
            var optDddBook = queries.getBook("Domain-Driven Design").join();
            var blochBooks = queries.getAuthorBooks(J_BLOCH).join();

            LOGGER.info("Author username : {}", optEmptyAuthor);
            optEvans.ifPresent(evans -> LOGGER.info("Author evans : {}", evans));
            LOGGER.info("jBloch number of books : {}", blochBooksCount);
            LOGGER.info("Number of authors : {}", authorsCount);
            optDddBook.ifPresent(dddBook -> LOGGER.info("DDD book : {}", dddBook));
            LOGGER.info("jBloch books : {}", blochBooks);
        } finally {
            DatabaseInit.destroy();
        }
    }
}
