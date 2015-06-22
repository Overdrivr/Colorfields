package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.Vector;

/**
 * Created by Bart on 14/06/2015.
 */
public class Convoy {
    private final GameEngine engine;
    Vector<Body> containers;

    boolean inCaptureSequence;

    // Constants
    float jointFrequency = 3f;
    float jointLength = 0.3f;
    float containerx = 0.05f;
    float containery = 0.1f;

    public Convoy(GameEngine e, Vector2 position, Vector2 orientation, Vector2 force, int amount) {
        engine = e;
        containers = new Vector<Body>();
        inCaptureSequence = false;
        float angle = (float)(Math.atan2(orientation.x,orientation.y));

        //First sphere
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.angle = -angle;
        Body body = e.world.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_ORE;
        data.convoy = this;
        body.setUserData(data);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(containerx,containery);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        body.createFixture(fixtureDef);
        body.applyLinearImpulse(force, position, true);

        containers.add(body);

        orientation.setLength(jointLength);

        //All following spheres
        for(int i = 0 ; i < amount ; i++){
            bodyDef.position.set(position.x - orientation.x * (i+1), position.y - orientation.y * (i+1));
            Body body2 = e.world.createBody(bodyDef);
            body2.setUserData(data);
            body2.createFixture(fixtureDef);

            // Revolution joint
            RevoluteJointDef jointDef2 = new RevoluteJointDef();
            jointDef2.enableLimit = true;
            jointDef2.lowerAngle = -0.52f;
            jointDef2.upperAngle = 0.52f;

            Vector2 anchor = new Vector2();
            anchor.x = (containers.lastElement().getPosition().x + body2.getPosition().x)/2;
            anchor.y = (containers.lastElement().getPosition().y + body2.getPosition().y)/2;

            jointDef2.initialize(containers.lastElement(), body2, anchor);
            e.world.createJoint(jointDef2);

            // Apply initial impulse to the body
            body2.applyLinearImpulse(force, body2.getPosition(), true);

            containers.add(body2);
        }

        shape.dispose();
    }

    public void update(GravityField field){
        if(!inCaptureSequence){
            boolean t = false;
            Vector2 force;
            Color color = new Color(1,1,1,1);

            for(Body c : containers){
                if(!t){
                    color.r = 0;
                    color.g = 1;
                    color.b = 0;
                    t = true;
                }
                else
                {
                    color.r = 0.2f;
                    color.g = 0;
                    color.b = 0;
                }
                force = field.getForce(c.getPosition().x,c.getPosition().y,color);
                c.applyForce(force,c.getPosition(),true);
            }
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

    public Vector2 getFirstPosition(){
        return  containers.firstElement().getPosition();
    }

    public void applyForceToFirst(Vector2 force){
        containers.firstElement().applyForce(force,containers.firstElement().getPosition(),true);
    }
}
