package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * Created by Bart on 13/06/2015.
 */

public class MyContactListener implements ContactListener {
    private final GameEngine engine;
    public MyContactListener(GameEngine e){
        super();
        engine = e;
    }

    @Override
    public void endContact(Contact contact) {
        Body A = contact.getFixtureA().getBody();
        Body B = contact.getFixtureB().getBody();
    }

    @Override
    public void beginContact(Contact contact) {
        Body A = contact.getFixtureA().getBody();
        Body B = contact.getFixtureB().getBody();

        MyBodyData dataA = (MyBodyData)(A.getUserData());
        MyBodyData dataB = (MyBodyData)(B.getUserData());

        if(dataA != null && dataB != null)
        {
            // Outer circle
            if(dataA.type == BodyType.BODY_TYPE_ORE && dataB.type == BodyType.BODY_TYPE_END){
                // An ore has reached the endpoint, tell the game engine
                engine.containerReachedEndpointLockArea(A);
            }
            if(dataB.type == BodyType.BODY_TYPE_ORE && dataA.type == BodyType.BODY_TYPE_END){
                engine.containerReachedEndpointLockArea(B);
            }
            // Inner circle
            if(dataA.type == BodyType.BODY_TYPE_ORE && dataB.type == BodyType.BODY_TYPE_END_DESTROY){
                // An ore has reached the inner endpoint, tell the game engine
                engine.containerReachedEndpointDestroyArea(A);
            }
            if(dataB.type == BodyType.BODY_TYPE_ORE && dataA.type == BodyType.BODY_TYPE_END_DESTROY){
                engine.containerReachedEndpointDestroyArea(B);
            }
        }
    }

    @Override
    public void preSolve(Contact contact,Manifold oldManifold){

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse){

    }
}
