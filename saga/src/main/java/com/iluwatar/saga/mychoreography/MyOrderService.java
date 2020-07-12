package com.iluwatar.saga.mychoreography;

public class MyOrderService extends MyService {

    public MyOrderService(final MyServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public String getName() {
        return "init an order";
    }
}