package com.iluwatar.asynccqrs.util;

import dev.miku.r2dbc.mysql.MySqlConnection;
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import reactor.core.publisher.Mono;

public class Database {

    private static final MySqlConnectionFactory CONNECTION_FACTORY = MySqlConnectionFactory.from(MySqlConnectionConfiguration.builder()
            .host("127.0.0.1")
            .user("root")
            .port(3306)
            .password("secret")
            .database("library")
            .build());

    public static Mono<MySqlConnection> connect() {

        return CONNECTION_FACTORY.create();
    }
}
