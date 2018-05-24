package com.uowee.observer;

public interface KeyPanelSubject {
    void registerObserver(KeyPanelObserver o);

    void removeObserver(KeyPanelObserver o);

    void notifyObservers(String type);
}
