package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Created by Bart on 02/07/2015.
 *
 *  This class detects whenever an object reaches world limits and destroys it
 */
public class WorldLimits {
    final GameEngine engine;
    public WorldLimits(final GameEngine e, float x_left, float x_right, float y_top, float y_bottom,float offset){
        engine = e;
        Vector2 topleft_corner = new Vector2(x_left - offset, y_top + offset);
        Vector2 topright_corner = new Vector2(x_right + offset, y_top + offset);
        Vector2 bottomleft_corner = new Vector2(x_left - offset, y_bottom - offset);
        Vector2 bottomright_corner = new Vector2(x_right + offset, y_bottom - offset);

        //Gdx.app.log("WorldLimits","topleft(x,y)"+topleft_corner.x+" ; "+topleft_corner.y);
        //Gdx.app.log("WorldLimits","topright(x,y)"+topright_corner.x+" ; "+topright_corner.y);

        createWall(topleft_corner,bottomleft_corner);
        createWall(topright_corner,bottomright_corner);
        createWall(topleft_corner,topright_corner);
        createWall(bottomleft_corner,bottomright_corner);
    }

    private void createWall(Vector2 pointA,Vector2 pointB){
        //Create body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0,0);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our body in the world using our body definition
        Body body = engine.world.createBody(bodyDef);

        // Set data
        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_WORLD_LIMITS;
        body.setUserData(data);

        EdgeShape shape = new EdgeShape();
        shape.set(pointA,pointB);
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
    }
}
