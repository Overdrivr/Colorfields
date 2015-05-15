package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bart on 26/04/2015.
 */
public class MyGestureListener implements GestureDetector.GestureListener {
    GameScreen gamescreen;

    public void setGamescreen(final GameScreen screen){
        gamescreen = screen;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        gamescreen.TouchDown();
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        gamescreen.Tap(x,y);
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {

        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        gamescreen.Fling(velocityX,velocityY);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        gamescreen.Pan(new Vector2(x,y),new Vector2(x+deltaX, y+deltaY));
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {

        return false;
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance){
        gamescreen.Zoom(originalDistance,currentDistance);
        return true;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        return false;
    }
}
