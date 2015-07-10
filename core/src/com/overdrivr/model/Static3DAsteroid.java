package com.overdrivr.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by Bart on 04/07/2015.
 */
public class Static3DAsteroid {
    final GameEngine engine;
    public Body body;

    public Model model;
    public ModelInstance instance;

    public Static3DAsteroid(GameEngine e){
        engine = e;

        // Attach the class to the stage
        //engine.stage.addActor(this);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(2,2,2,10,10,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model,0,3,0);
    }

    public void render(ModelBatch batch){
        batch.render(instance);
    }

    public void dispose(){
        model.dispose();
    }

}
