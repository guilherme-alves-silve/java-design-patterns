package com.iluwatar.saga.mychoreography;

public class MyHotelBookingService extends MyService {

    public MyHotelBookingService(final MyServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    public String getName() {
        return "booking a Hotel";
    }
}
