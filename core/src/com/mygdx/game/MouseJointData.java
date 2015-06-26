package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * Created by B48923 on 6/26/2015.
 */
public class MouseJointData {
    Convoy convoy;
    Body fixedBody;
    MouseJointDef jointDef;

    public MouseJointData(){
        jointDef = new MouseJointDef();
    }
}
