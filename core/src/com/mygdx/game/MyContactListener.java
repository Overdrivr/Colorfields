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

        //Gdx.app.log("endContact", "between " + fixtureA.toString() + " and " + fixtureB.toString());
    }

    @Override
    public void beginContact(Contact contact) {
        Body A = contact.getFixtureA().getBody();
        Body B = contact.getFixtureB().getBody();

        MyBodyData dataA = (MyBodyData)(A.getUserData());
        MyBodyData dataB = (MyBodyData)(B.getUserData());

        if(dataA != null && dataB != null)
        {
            if(dataA.type == BodyType.BODY_TYPE_ORE && dataB.type == BodyType.BODY_TYPE_END){
                engine.oreReachedEndpoint(A);
            }
            if(dataB.type == BodyType.BODY_TYPE_ORE && dataA.type == BodyType.BODY_TYPE_END){
                // An ore has reached the endpoint, tell the game engine
                engine.oreReachedEndpoint(B);
                //Gdx.app.log("beginContact", "between " + A.toString() + " and " + B.toString());
            }
        }

        //Gdx.app.log("Type",fixtureA.getUserData().toString()+" : "+fixtureB.getUserData().toString());
    }

    @Override
    public void preSolve(Contact contact,Manifold oldManifold){

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse){

    }
}
