package com.iluwatar.mycqrs.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MemoryDatabase {

    private static final ConcurrentMap<String, ConcurrentMap<Integer, Object>> DB;

    private static final ConcurrentMap<String, Class<?>> TABLE_TYPE_CHECK;

    private static final AtomicInteger ID;

    private static final MemoryDatabase memoryDatabase;

    static {
        DB = new ConcurrentHashMap<>();
        TABLE_TYPE_CHECK = new ConcurrentHashMap<>();
        ID = new AtomicInteger();
        memoryDatabase = new MemoryDatabase();
    }

    public static MemoryDatabase db() {
        return memoryDatabase;
    }

    public void save(final String tableName, final Object object) {

        checkTableType(tableName, object.getClass());
        final var table = getTable(tableName);
        table.put(ID.incrementAndGet(), object);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String tableName, final Integer id) {
        final var table = getTable(tableName);
        return (T) table.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> getList(final String tableName) {
        final var table = getTable(tableName);
        return (Stream<T>) table.values().stream();
    }

    private synchronized ConcurrentMap<Integer, Object> getTable(final String tableName) {
        var table = DB.get(tableName);
        if (null == table) {
            table = new ConcurrentHashMap<>();
            DB.put(tableName, table);
        }

        return table;
    }

    private synchronized void checkTableType(final String tableName, final Class<?> objectType) {
        var type = TABLE_TYPE_CHECK.computeIfAbsent(tableName, key -> objectType);

        if (!TABLE_TYPE_CHECK.get(tableName).equals(objectType)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid object type %s for the table %s, the accepted type is %s",
                    objectType.getSimpleName(),
                    tableName,
                    type.getSimpleName()
            ));
        }
    }
}
