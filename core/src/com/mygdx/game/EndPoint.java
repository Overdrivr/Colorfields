package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * Created by Bart on 18/06/2015.
 */
public class EndPoint {
    final GameEngine gameEngine;
    private boolean captureSequenceOnGoing = false;
    private Convoy convoy;
    private Vector2 m_position;
    private float m_radius;
    float coeff_P = 0.03f;
    float coeff_D = 0.03f;
    Body body;

    Body body_center;

    public EndPoint(final GameEngine e, Vector2 position, float radius){
        m_position = position;
        m_radius = radius;
        gameEngine = e;

        // Init the end point
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);

        body = gameEngine.world.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_END;
        body.setUserData(data);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        //Create center body
        shape.setRadius(radius/10);
        fixtureDef.isSensor = false;
        body_center = gameEngine.world.createBody(bodyDef);
        body_center.createFixture(fixtureDef);

        shape.dispose();
    }
    public void startCapture(Convoy c){
        if(!captureSequenceOnGoing){
            captureSequenceOnGoing = true;
            c.inCaptureSequence = true;
            convoy = c;

            //Create joint distance
            //TODO : Truc qui merde
            MouseJointDef jointDef = new MouseJointDef();
            //jointDef.frequencyHz = 2;
            jointDef.bodyA = body_center;
            jointDef.bodyB = c.containers.firstElement();
            //jointDef.frequencyHz = 200;
            jointDef.maxForce = 1000*c.containers.firstElement().getMass();
            //jointDef.dampingRatio = 0.5f;
            jointDef.target.set(6.f,2.f);
            //jointDef.collideConnected = true;
            // Put the joint definition in joints-to-build list
            // Because it is forbidden to add/remove joints/bodies during box2d step function
            gameEngine.jointsToBuild.push(jointDef);
        }
    }
    public void update(){
        if(captureSequenceOnGoing){
            //Get position of first container
            /*Vector2 firstPos = convoy.containers.firstElement().getPosition();
            //Compute traction force
            Vector2 p = new Vector2(m_position.x-firstPos.x,m_position.y-firstPos.y);
            Vector2 force = new Vector2();
            force.x = p.x * coeff_P;
            force.y = p.y * coeff_P;

            //Apply to convoy
            convoy.applyForceToFirst(force);*/
        }
    }
}
