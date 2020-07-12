package com.iluwatar.saga.mychoreography;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MyServiceDiscovery {

    private final Map<String, MyChoreographyChapter> services;

    private MyServiceDiscovery() {
        this.services = new HashMap<>();
    }

    public MyChoreographyChapter findAny() {
        return services.values().iterator().next();
    }

    public Optional<MyChoreographyChapter> find(final String name) {
        return Optional.ofNullable(services.get(name));
    }

    public MyServiceDiscovery discover(final MyChoreographyChapter choreographyChapter) {
        Objects.requireNonNull(choreographyChapter, "choreographyChapter cannot be null!");
        services.put(choreographyChapter.getName(), choreographyChapter);
        return this;
    }

    public static MyServiceDiscovery create() {
        return new MyServiceDiscovery();
    }
}
