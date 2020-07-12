package com.iluwatar.saga.mychoreography;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class MyService implements MyChoreographyChapter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MyService.class);

    private final MyServiceDiscovery discoveryService;

    public MyService(final MyServiceDiscovery discoveryService) {
        this.discoveryService = Objects.requireNonNull(discoveryService, "discoveryService cannot be null!");
    }

    @Override
    public MySaga execute(MySaga saga) {

        final var chapter = saga.getCurrent();
        final var chapterName = chapter.getName();
        var nextSaga = saga;
        Object value = null;
        if (chapterName.equals(getName())) {

            if (saga.isForward()) {

                nextSaga = process(saga);
                value = nextSaga.getCurrentValue();
                if (nextSaga.isCurrentSuccess()) {
                    nextSaga.forward();
                } else {
                    nextSaga.mustRollback();
                }
            } else {

                nextSaga = rollback(nextSaga);
                value = nextSaga.getCurrentValue();
                nextSaga.back();
            }

            if (isSagaFinished(nextSaga)) {
                return nextSaga;
            }

            nextSaga.setCurrentValue(value);
        }

        final var finalNextSaga = nextSaga;
        return discoveryService.find(nextSaga.getCurrent().getName())
                .map(nextChapter -> nextChapter.execute(finalNextSaga))
                .orElseThrow(serviceNotFoundException(chapterName));
    }

    private Supplier<RuntimeException> serviceNotFoundException(String chapterName) {
        return () -> new RuntimeException(
                String.format("the service %s has not been found", chapterName));
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
