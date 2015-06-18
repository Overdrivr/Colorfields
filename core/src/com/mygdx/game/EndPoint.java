package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Bart on 18/06/2015.
 */
public class EndPoint {
    private boolean captureSequenceOnGoing = false;
    private Convoy convoy;
    private Vector2 m_position;
    private float m_radius;
    float coeff_P = 0.03f;
    float coeff_D = 0.03f;

    public EndPoint(final World w, Vector2 position, float radius){
        m_position = position;
        m_radius = radius;

        // Init the end point
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);

        Body body = w.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_END;
        body.setUserData(data);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        shape.dispose();
    }
    public void startCapture(Convoy c){
        captureSequenceOnGoing = true;
        c.inCaptureSequence = true;
        convoy = c;
    }
    public void update(){
        if(captureSequenceOnGoing){
            //Get position of first container
            Vector2 firstPos = convoy.containers.firstElement().getPosition();
            //Compute traction force
            Vector2 p = new Vector2(m_position.x-firstPos.x,m_position.y-firstPos.y);
            Vector2 force = new Vector2();
            force.x = p.x * coeff_P;
            force.y = p.y * coeff_P;

            //Apply to convoy
            convoy.applyForceToFirst(force);
        }
    }
}
