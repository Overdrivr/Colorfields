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
        gamescreen.Fling(0.f,0.f);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {

        return false;
    }

    @Override
    public boolean longPress(float x, float y) {

        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        Gdx.app.log("SPEED",Float.toString(velocityX));
        /*Vector2 v = new Vector2(velocityX,velocityY);
        v.nor();
        v.x *= 1500.f;
        v.y *= 1500.f;
        gamescreen.Fling(-v.x,v.y);*/
        gamescreen.Fling(-velocityX,velocityY);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        gamescreen.RawPanView(-deltaX,deltaY);
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {

        return false;
    }

    @Override
    public boolean zoom (float originalDistance, float currentDistance){
        gamescreen.Zoom(originalDistance, currentDistance);
        return true;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){

        return false;
    }
}
