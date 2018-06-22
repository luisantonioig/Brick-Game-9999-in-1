package com.github.vitalibo.brickgame.core;

import java.net.Socket;

public interface Controllable {

    public Socket socket = null;

    void doDown();
    void doLeft();
    void doRight();
    void doUp();

    void doRotate();

}