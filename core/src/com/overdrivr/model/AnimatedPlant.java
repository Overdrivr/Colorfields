package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Bart on 17/07/2015.
 */
public class AnimatedPlant {
    private final GameEngine engine;
    public ModelInstance modelInstance;
    private final static BoundingBox bounds = new BoundingBox();
    float angle = 90;
    public AnimatedPlant(GameEngine e){
        engine = e;
        modelInstance = new ModelInstance(engine.assetManager.get("Models/B1/B1.g3db", Model.class));

        modelInstance.transform.translate(0, 0, 0);
        modelInstance.transform.rotate(1,0,0,angle);
        modelInstance.transform.scale(0.05f,0.05f,0.05f);
        modelInstance.calculateBoundingBox(bounds);
    }

    public void render(ModelBatch batch){
        batch.render(modelInstance);
    }
}
