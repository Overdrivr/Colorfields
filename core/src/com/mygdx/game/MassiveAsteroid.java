package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import java.awt.font.TransformAttribute;

/**
 * Created by Bart on 14/05/2015.
 */
public class MassiveAsteroid extends Actor {
    GameEngine g;
    Texture texture;
    Sprite sprite;
    public Body body;

    public MassiveAsteroid(final GameEngine game, String filename, Vector2 position){
        g = game;

        //Get pixmap
        g.assetManager.load(filename, Pixmap.class);

        //Gdx.app.log("MassiveAsteroids", "Loading asset");
        do{
            //Gdx.app.log("MassiveAsteroids", Float.toString(g.assetManager.getProgress()));
        }while(!g.assetManager.update());

        if(g.assetManager.isLoaded(filename)){
            Gdx.app.log("MassiveAsteroids","Loaded pixmap");
            // Get the pixmap
            Pixmap p = g.assetManager.get(filename,Pixmap.class);

            // Create the sprite
            texture = new Texture(p);
            sprite = new Sprite(texture);

            //Detect contour
            Array<Vector2> v = g.converter.marchingSquares(p);

            // Simplify contour
            Array<Vector2> w = g.converter.RDP(v, 3.f);
            w.reverse();

            //Create body
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(position);


            // Create a fixture definition to apply our shape to
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.density = 0.5f;
            fixtureDef.friction = 0.8f;
            fixtureDef.restitution = 0.6f; // Make it bounce a little bit

            // Create our body in the world using our body definition
            body = g.world.createBody(bodyDef);

            g.triangulator.BuildShape(body,fixtureDef,w);

            // Generate convex hull from simplified contour
            //Vector2[] z = w.toArray(Vector2.class);

            //Gdx.app.log("Convex Hull check",Integer.toString(splitter.validate(z)));
            //splitter.separate(body,fixtureDef,z);

            //Gdx.app.log("Convex Hull","generated");
        }
    }

    public void draw(Batch batch){
        // Coin en haut a gauche de la box aligne avec centre sprite
        /*sprite.setPosition(body.getPosition().x - sprite.getWidth()/2.f,
                           body.getPosition().y - sprite.getHeight()/2.f);*/

        sprite.setPosition(body.getPosition().x,
                body.getPosition().y - sprite.getHeight());
        sprite.setOrigin(0, 0);
        //sprite.setRotation((float) (Math.toDegrees(body.getAngle())));

        sprite.draw(batch);
        /*batch.draw(sprite,
                sprite.getX(), sprite.getY(),
                0,0,
                sprite.getWidth(),sprite.getHeight(),
                sprite.getScaleX(),sprite.getScaleY(),
                sprite.getRotation());*/
        /*batch.draw(texture, body.getPosition().x, body.getPosition().y,
                0, 0,
                texture.getWidth(), texture.getHeight(),
                1.f, 1.f, (float) (Math.toDegrees(body.getAngle())),
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false,false);*/
    }
}
