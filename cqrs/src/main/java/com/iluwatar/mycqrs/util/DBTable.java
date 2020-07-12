package com.iluwatar.mycqrs.util;

public class DBTable {

    private DBTable() {
        throw new IllegalArgumentException("No DBTable!");
    }

    public static final String AUTHOR_TBL = "author_tbl";
    public static final String BOOK_TBL = "book_tbl";
}
