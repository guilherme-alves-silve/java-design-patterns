package com.iluwatar.saga.mychoreography;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class MyService implements MyChoreographyChapter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MyService.class);

    private final MyDiscoveryService discoveryService;

    public MyService(final MyDiscoveryService discoveryService) {
        this.discoveryService = Objects.requireNonNull(discoveryService, "discoveryService cannot be null!");
    }

    @Override
    public MySaga execute(MySaga saga) {
        return null;
    }

    @Override
    public MySaga process(MySaga saga) {

        final var value = saga.getCurrentValue();

        LOGGER.info("The chapter '{}' has been started. "
                + "The data {} has been stored or calculated successfully",
        getName(), value);

        return saga.setCurrentValue(value)
                    .setCurrentStatus(MySaga.MyChapterStatus.SUCCESS);
    }

    @Override
    public MySaga rollback(MySaga saga) {
        final var value = saga.getCurrentValue();

        LOGGER.info("The rollback of chapter '{}' has been started. "
                        + "The data {} has been rollbacked successfully",
                getName(), value);

        return saga.setCurrentValue(value)
                .setCurrentStatus(MySaga.MyChapterStatus.ROLLBACK);
    }

    private boolean isSagaFinished(final MySaga saga) {
        if (!saga.inRange()) {
            saga.setFinished(true);
            LOGGER.info("Saga finished with {} status", saga.getResult());
        }

        return saga.isFinished();
    }
}
