package com.uowee.observer;

import java.awt.Image;


public interface ScreenSubject {
    void registerScreenObserver(AndroidScreenObserver o);

    void removeScreenObserver(AndroidScreenObserver o);

    void notifyScreenObservers(Image image);

}
