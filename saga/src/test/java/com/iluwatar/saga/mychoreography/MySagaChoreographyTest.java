package com.iluwatar.saga.mychoreography;

import org.junit.Assert;
import org.junit.Test;

public class MySagaChoreographyTest {

    @Test
    public void executeTest() {
        var serviceDiscovery = serviceDiscovery();
        var service = serviceDiscovery.findAny();
        var rollbackOrderSaga = service.execute(newSaga("must_rollback_order"));
        var finishOrderSaga = service.execute(newSaga("must_finish_order"));

        Assert.assertEquals(rollbackOrderSaga.getResult(), MySaga.MyResult.ROLLBACK);
        Assert.assertEquals(finishOrderSaga.getResult(), MySaga.MyResult.FINISHED);
    }

    private static MySaga newSaga(Object value) {
        return MySaga
                .create()
                .chapter("init an order").setInValue(value)
                .chapter("booking a Fly")
                .chapter("booking a Hotel")
                .chapter("withdrawing Money");
    }

    private static MyServiceDiscovery serviceDiscovery() {
        var serviceDiscovery = MyServiceDiscovery.create();
        return serviceDiscovery
                .discover(new MyOrderService(serviceDiscovery))
                .discover(new MyFlyBookingService(serviceDiscovery))
                .discover(new MyHotelBookingService(serviceDiscovery))
                .discover(new MyWithdrawMoneyService(serviceDiscovery));
    }
}
