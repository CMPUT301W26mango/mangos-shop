package com.example.myapplication;

public interface OnEventLoadedListener {
    void onEventLoaded(Event event);
    void onError(String message);
}
