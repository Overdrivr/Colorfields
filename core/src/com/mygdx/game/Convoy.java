package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

import java.util.Vector;

/**
 * Created by Bart on 14/06/2015.
 */
public class Convoy {
    private final GameEngine engine;
    private Vector<Body> containers;

    // Constants
    float jointFrequency = 3f;
    float jointLength = 0.3f;
    float jointOffsetx = 0.05f;// Must be <= than containerx
    float jointOffsety = 0.1f;// Must be <= than containery
    float containerx = 0.05f;
    float containery = 0.1f;


    public Convoy(GameEngine e, Vector2 position, Vector2 orientation, Vector2 force, int amount) {
        engine = e;
        containers = new Vector<Body>();

        //First sphere
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.angle = 0;
        Body body = e.world.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_ORE;
        body.setUserData(data);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(containerx,containery);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        Fixture f = body.createFixture(fixtureDef);
        body.applyLinearImpulse(force, position, true);

        containers.add(body);

        orientation.setLength(jointLength);


        // Lateral offsets of joints in container ref
        /*Vector2 offsetA1 = ortho(orientation,true);
        Vector2 offsetA2 = ortho(orientation,false);

        offsetA1.setLength(jointOffsetx);
        offsetA2.setLength(jointOffsetx);

        // Vertical offsets of joints in container ref
        Vector2 offsetB1 = orientation;
        Vector2 offsetB2 = orientation;
        offsetB1.setLength(containery);
        offsetB2.setLength(-containery);*/


        //All following spheres
        for(int i = 0 ; i < amount ; i++){
            bodyDef.position.set(position.x - orientation.x * (i+1), position.y - orientation.y * (i+1));
            Body body2 = e.world.createBody(bodyDef);
            Fixture f2 = body2.createFixture(fixtureDef);

            //left joint
            DistanceJointDef jointDef = new DistanceJointDef();
            jointDef.frequencyHz = jointFrequency;
            jointDef.length = jointLength;

            Vector2 jointStart = new Vector2();
            jointStart.x = containers.lastElement().getPosition().x + containerx;
            jointStart.y = containers.lastElement().getPosition().y + containery;

            Vector2 jointEnd = new Vector2();
            jointEnd.x = body2.getPosition().x + containerx;
            jointEnd.y = body2.getPosition().y - containery;

            jointDef.initialize(containers.lastElement(), body2, jointStart, jointEnd);
            e.world.createJoint(jointDef);

            // Right joint
            jointStart.x = containers.lastElement().getPosition().x - containerx;
            jointStart.y = containers.lastElement().getPosition().y + containery;

            jointEnd.x = body2.getPosition().x - containerx;
            jointEnd.y = body2.getPosition().y - containery;

            jointDef.initialize(containers.lastElement(), body2, jointStart, jointEnd);
            e.world.createJoint(jointDef);

            // Apply initial impulse to the body
            body2.applyLinearImpulse(force, body2.getPosition(), true);

            containers.add(body2);
        }

        shape.dispose();
    }

    public void update(GravityField field){
        for(Body c : containers){
            Vector2 force = field.getForce(c.getPosition().x,c.getPosition().y);
            c.applyForce(force,c.getPosition(),true);
        }
    }

    private Vector2 ortho(Vector2 a, boolean left){
        Vector3 u = new Vector3(a.x,a.y,0);
        Vector3 v = left ? new Vector3(0,0,1) : new Vector3(0,0,-1);

        Vector2 result = new Vector2();
        result.x = u.y * v.z - u.z * v.y;
        result.y = u.z * v.x - u.x * v.z;

        return result;
    }
}
