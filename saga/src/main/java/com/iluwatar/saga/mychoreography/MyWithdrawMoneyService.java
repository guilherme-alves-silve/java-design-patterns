package com.iluwatar.saga.mychoreography;

public class MyWithdrawMoneyService extends MyService {

    public MyWithdrawMoneyService(final MyServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public String getName() {
        return "withdrawing Money";
    }

    @Override
    public MySaga process(MySaga saga) {

        final var value = saga.getCurrentValue();
        if (value.equals("must_rollback_order")) {

            LOGGER.info("The chapter '{}' failed to process data '{}', must rollback. ", getName(), value);

            return saga.setCurrentValue(value)
                    .setCurrentStatus(MySaga.MyChapterStatus.ROLLBACK);
        }

        return super.process(saga);
    }
}
