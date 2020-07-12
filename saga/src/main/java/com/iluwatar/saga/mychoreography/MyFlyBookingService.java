package com.iluwatar.saga.mychoreography;

public class MyFlyBookingService extends MyService {

    public MyFlyBookingService(final MyServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public String getName() {
        return "booking a Fly";
    }
}
