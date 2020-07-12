package com.iluwatar.saga.mychoreography;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MyDiscoveryService {

    private final Map<String, MyChoreographyChapter> services;

    public MyDiscoveryService() {
        this.services = new HashMap<>();
    }

    public MyChoreographyChapter findAny() {
        return services.values().iterator().next();
    }

    public Optional<MyChoreographyChapter> find(final String name) {
        return Optional.ofNullable(services.get(name));
    }

    public MyDiscoveryService discover(final MyChoreographyChapter choreographyChapter) {
        Objects.requireNonNull(choreographyChapter, "choreographyChapter cannot be null!");
        services.put(choreographyChapter.getName(), choreographyChapter);
        return this;
    }
}
