package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by Bart on 14/05/2015.
 */
public class MassiveAsteroid extends Actor {
    final GameEngine g;
    public Body body;
    //Group root;

    Pixmap pixmap;
    Texture texture;
    TextureRegion textureRegion;
    Sprite sprite;
    //Image image;

    public MassiveAsteroid(final GameEngine game, String filename, Vector2 position, float distance){
        g = game;

        //Load asset
        g.assetManager.load(filename, Pixmap.class);
        g.assetManager.finishLoading();

        if(!g.assetManager.isLoaded(filename))
            throw new GdxRuntimeException("File "+filename+"impossible to load.");

        // Get the assets
        pixmap = g.assetManager.get(filename,Pixmap.class);
        texture = new Texture(pixmap);
        textureRegion = new TextureRegion(texture);
        textureRegion.flip(false,true);
        sprite = new Sprite(textureRegion);

        //Detect contour from pixmap
        Array<Vector2> raw_contour = g.converter.marchingSquares(pixmap);

        for(int i = 0 ; i < raw_contour.size ; i++){
            raw_contour.get(i).x *= 0.01f;
            raw_contour.get(i).y *= 0.01f;
        }

        // Simplify contour
        Array<Vector2> simplified_contour = g.converter.RDP(raw_contour, distance);
        // Reverse order so that points run clockwise
        simplified_contour.reverse();

        //Define body properties
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);

        // Create a fixture definition base for all polygon shape
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our body in the world using our body definition
        body = g.world.createBody(bodyDef);

        // convert Array<Vector2> to FloatArray
        // For some reason the first vertice messes up two different triangulation algorithm
        // Remove it (maybe the first and last detected points of contour are identical)
        FloatArray array = new FloatArray();
        for(int i = 1 ; i < simplified_contour.size ; i++){
            array.add(simplified_contour.get(i).x);
            array.add(simplified_contour.get(i).y);
        }

        // Create the polygon shapes from the contour and attach them to the body
        g.triangulator.BuildShape(body, fixtureDef, array);

        sprite.setOrigin(0,0);
        sprite.setScale(0.01f);
    }

    public void draw(SpriteBatch batch){
        //sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight()/2);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
        sprite.setRotation((float) (Math.toDegrees(body.getAngle())));
        sprite.draw(batch);
    }

    // Dispose () ? Remove the root and image from stage
}
