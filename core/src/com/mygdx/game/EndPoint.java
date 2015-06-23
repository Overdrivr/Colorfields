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
    public Vector2 m_position;
    Body body;

    public EndPoint(final GameEngine e, Vector2 position, float radius){
        m_position = position;
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

        shape.dispose();
    }
    public void startCapture(Convoy c){
        if(!c.inCaptureSequence){
            c.inCaptureSequence = true;

            MouseJointDef jointDef = new MouseJointDef();
            jointDef.bodyA = body;
            jointDef.bodyB = c.containers.firstElement();
            jointDef.frequencyHz = 10;
            jointDef.maxForce = 20*c.containers.firstElement().getMass();
            jointDef.dampingRatio = 10.f;
            jointDef.target.set(c.containers.firstElement().getPosition());
            jointDef.collideConnected = true;
            // Put the joint definition in joints-to-build list
            // Because it is forbidden to add/remove joints/bodies during box2d step function
            gameEngine.jointsToBuild.push(jointDef);
        }
    }
}
