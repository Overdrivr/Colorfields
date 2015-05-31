package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;

/**
 * Created by Bart on 31/05/2015.
 */
public class MyGestureDetector extends GestureDetector {
    public MyGestureDetector (MyGestureListener listener) {
        super(listener);
    }

    @Override
    public boolean touchUp(float x, float y, int pointer, int button){
        // Get time
        Gdx.app.log("TIME2", Long.toString(Gdx.input.getCurrentEventTime()));
        return super.touchUp(x, y, pointer, button);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button){
        Gdx.app.log("TIME2", Long.toString(Gdx.input.getCurrentEventTime()));
        return super.touchDown(x,y,pointer,button);
    }
}
