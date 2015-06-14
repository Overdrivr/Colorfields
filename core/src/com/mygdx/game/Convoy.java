package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

import java.util.Vector;

/**
 * Created by Bart on 14/06/2015.
 */
public class Convoy {
    private final GameEngine engine;
    private Vector<Body> containers;

    public Convoy(GameEngine e, Vector2 position, Vector2 orientation, Vector2 force, int amount) {
        engine = e;
        containers = new Vector<Body>();

        //First sphere
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        Body body = e.world.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_ORE;
        body.setUserData(data);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.05f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        body.createFixture(fixtureDef);
        body.applyLinearImpulse(force, position, true);

        containers.add(body);

        //All following spheres
        for(int i = 0 ; i < amount ; i++){
            bodyDef.position.set(position.x - orientation.x * (i+1), position.y - orientation.y * (i+1));
            Body body2 = e.world.createBody(bodyDef);
            Fixture f2 = body2.createFixture(fixtureDef);

            //Joint
            DistanceJointDef jointDef = new DistanceJointDef();
            jointDef.frequencyHz = 1.5f;
            jointDef.initialize(containers.lastElement(), body2, containers.lastElement().getPosition(), body2.getPosition());
            e.world.createJoint(jointDef);

            body2.applyLinearImpulse(force, body2.getPosition(), true);

            containers.add(body2);
        }

        circle.dispose();
    }

    public void update(GravityField field){
        for(Body c : containers){
            Vector2 force = field.getForce(c.getPosition().x,c.getPosition().y);
            c.applyForce(force,c.getPosition(),true);
        }
    }
}
