package com.mygdx.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by B48923 on 7/1/2015.
 */
public class ConvoyUnit extends Actor {
    Sprite sprite;
    private final GameEngine engine;
    int unit_type;
    Convoy convoy;
    public Body body;

    public ConvoyUnit(GameEngine e, Convoy c, int type, Vector2 position,
                      float angle, Vector2 initialImpulse){
        engine = e;
        unit_type = type;
        convoy = c;

        // Init physics
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.angle = -angle;
        body = e.world.createBody(bodyDef);

        MyBodyData data = new MyBodyData();
        data.convoy = c;
        data.type = BodyType.BODY_TYPE_CHARACTER;
        data.unit = this;
        body.setUserData(data);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(c.containerx,c.containery);// TODO : DO NOT USE PARENTS PARAMETERS

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        body.createFixture(fixtureDef);
        body.applyLinearImpulse(initialImpulse, position, true);

        shape.dispose();

        // Init graphics

        if(type >= engine.imagename_lookup.size || type < 0)
            throw new GdxRuntimeException("ConvoyUnit : Requested texture id : "+type+" is illegal");

        String imagename = engine.imagename_lookup.get(type);

        //Load asset
        engine.assetManager.load(imagename, Texture.class);
        // TODO : check that assets are not loaded at runtime
        engine.assetManager.finishLoading();

        if(!engine.assetManager.isLoaded(imagename))
            throw new GdxRuntimeException("File "+imagename+"impossible to load.");

        // Get the assets
        Texture texture = engine.assetManager.get(imagename,Texture.class);
        TextureRegion t = new TextureRegion(texture);
        sprite = new Sprite(texture);
        float factor = 0.01f;
        sprite.setScale(factor);
        sprite.setOriginCenter();// Utile ?

        // Attach the image to the stage
        engine.stage.addActor(this);
    }

    public void draw(Batch batch, float parentAlpha) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight()/2);
        sprite.setRotation((float) (Math.toDegrees(body.getAngle())));
        sprite.draw(batch, parentAlpha);
    }
    
    public void dispose(){
        // Remove from physics
        engine.world.destroyBody(body);
        // Remove from display
        this.remove();
    }
}
