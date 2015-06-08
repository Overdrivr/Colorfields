package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;

/**
 * Created by Bart on 31/05/2015.
 */
public class MyGestureDetector extends GestureDetector {
    final GameScreen myScreen;
    public MyGestureDetector (MyGestureListener listener, GameScreen screen) {
        super(listener);
        myScreen = screen;
    }

    @Override
    public boolean touchUp(float x, float y, int pointer, int button){
        myScreen.touchUpAction(x,y);
        return super.touchUp(x, y, pointer, button);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button){
        return super.touchDown(x,y,pointer,button);
    }
}
