package com.iluwatar.cqrsasync.util;

import dev.miku.r2dbc.mysql.MySqlResult;
import dev.miku.r2dbc.mysql.MySqlStatement;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DatabaseUtils {

    private DatabaseUtils() {
        throw new IllegalArgumentException("No DatabaseUtils!");
    }

    public static CompletableFuture<Boolean> executeStatement(final String sql, final Map<Integer, Object> toBind) {

        return Database.connect()
                .map(connection -> Mono.from(bind(connection.createStatement(sql), toBind)
                                    .execute())
                                    .single()
                                    .flatMap(MySqlResult::getRowsUpdated)
                                    .map(rows -> rows > 0)
                                    .doFinally(signalType -> connection.close()))
                .flatMap(Function.identity())
                .toFuture();
    }

    public static <T> Mono<T> executeFetchMono(final String sql, final Map<Integer, Object> toBind, BiFunction<Row, RowMetadata, T> mapper) {

        return Database.connect()
                .map(connection -> Mono.from(bind(connection.createStatement(sql), toBind)
                        .execute())
                        .flatMap(result -> Mono.from(result.map(mapper)))
                        .single()
                        .doFinally(signalType -> connection.close()))
                .flatMap(Function.identity());
    }

    public static <T> CompletableFuture<List<T>> executeFetchAll(final String sql, final Map<Integer, Object> toBind, BiFunction<Row, RowMetadata, T> mapper) {

        return Database.connect()
                .map(connection -> Flux.from(bind(connection.createStatement(sql), toBind)
                        .execute())
                        .flatMap(result -> Flux.from(result.map(mapper)))
                        .doFinally(signalType -> connection.close()))
                .flatMap(Flux::collectList)
                .toFuture();
    }

    public static <T> CompletableFuture<T> executeFetch(final String sql, final Map<Integer, Object> toBind, BiFunction<Row, RowMetadata, T> mapper) {
        return executeFetchMono(sql, toBind, mapper)
                .toFuture();
    }

    private static MySqlStatement bind(MySqlStatement statement, Map<Integer, Object> map) {
        final var orderedMap = new TreeMap<>(map);
        orderedMap.forEach(statement::bind);
        return statement;
    }
}
