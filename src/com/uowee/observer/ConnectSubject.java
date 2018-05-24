package com.uowee.observer;


public interface ConnectSubject {
    void registerConnectObserver(AndroidConnectObserver o);

    void removeConnectObserver(AndroidConnectObserver o);

    void notifyConnectObservers();

}
