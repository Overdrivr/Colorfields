package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.Vector;

/**
 * Created by Bart on 14/06/2015.
 */
public class Convoy{
    private final GameEngine engine;
    public Vector<ConvoyUnit> containers;

    boolean inCaptureSequence;

    ////////////////////////////////////// Constants
    float jointFrequency = 3f;
    float jointLength = 0.3f;
    float containerx = 0.05f;
    float containery = 0.1f;
    float convoy_lowerAngle = -0.52f;
    float convoy_upperAngle = 0.52f;
    ////////////////////////////////////////////////

    public Convoy(GameEngine e, Vector2 position, Vector2 orientation, Vector2 force, int amount) {
        engine = e;
        containers = new Vector<ConvoyUnit>();
        inCaptureSequence = false;
        float angle = (float)(Math.atan2(orientation.x,orientation.y));
        orientation.setLength(jointLength);

        //First character
        containers.add(new ConvoyUnit(engine,this,1,position,angle,force));

        //All following characters if need
        for(int i = 0 ; i < amount ; i++){

            Vector2 p  = new Vector2(position.x - orientation.x * (i+1), position.y - orientation.y * (i+1));

            ConvoyUnit c = new ConvoyUnit(engine, this, 0, p, angle, force);

            // Revolution joint
            RevoluteJointDef jointDef2 = new RevoluteJointDef();
            jointDef2.enableLimit = true;
            jointDef2.lowerAngle = convoy_lowerAngle;
            jointDef2.upperAngle = convoy_upperAngle;

            Vector2 anchor = new Vector2();
            anchor.x = (containers.lastElement().body.getPosition().x + c.body.getPosition().x)/2;
            anchor.y = (containers.lastElement().body.getPosition().y + c.body.getPosition().y)/2;

            jointDef2.initialize(containers.lastElement().body, c.body, anchor);
            Joint j = e.world.createJoint(jointDef2);

            // Store the new ConvoyUnit c
            containers.add(c);
        }
    }

    public void update(GravityField field){
        if(!inCaptureSequence){
            boolean t = false;
            Vector2 force;
            Color color = new Color(1,1,1,1);

            for(ConvoyUnit c : containers){
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
                force = field.getForce(c.body.getPosition().x,c.body.getPosition().y,color);
                c.body.applyForce(force, c.body.getPosition(), true);
            }
        }
    }

    public void draw(SpriteBatch batch2D){
        for(ConvoyUnit u : containers)
            u.draw(batch2D);
    }

    public void Remove(ConvoyUnit unit){
        // Check first that the unit exists
        if(containers.indexOf(unit) < 0){
            Gdx.app.log("Convoy","Unit not found");
            return;
        }

        unit.dispose();
        containers.remove(unit);
        // TODO : If container is cut in half, create a new one and move the objects into it ?
    }

    public void Destroy(){
        // Remove all box2d objects
        for(ConvoyUnit c : containers)
        {
            c.dispose();
        }
        containers.clear();
    }
}
