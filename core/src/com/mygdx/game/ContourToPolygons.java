package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bart on 14/05/2015.
 */
public class ContourToPolygons{
    IntArray discardedPoints;
    Array<Vector2> thePoly;
    Array<Array<Vector2>> result;

    EarClippingTriangulator triangulator;

    public void BuildShape(Body body, FixtureDef fixtureDef, FloatArray vertices){

        triangulator = new EarClippingTriangulator();

        ShortArray array = triangulator.computeTriangles(vertices);

        PolygonShape polyShape = new PolygonShape();

        // Iterate over triangles
        for(int i = 0 ; i < array.size/3 ; i++)
        {
            float[] points = new float[6];
            //Gdx.app.log("ContourToPolygons","---------- i = "+i);
            for (int j = 0 ; j < 3 ; j++){
                // Get index of the pair of point
                int index = array.get(i*3+j);
                // Get each point's coordinates
                points[j*2] = vertices.get(index*2);
                points[j*2+1] = vertices.get(index*2+1);
                //Gdx.app.log("ContourToPolygons"," j = "+j);
                //Gdx.app.log("ContourToPolygons"," index = "+index);
                //Gdx.app.log("ContourToPolygons"," x = "+points[j*2]);
                //Gdx.app.log("ContourToPolygons"," y = "+points[j*2+1]);
            }
            polyShape.set(points);
            fixtureDef.shape = polyShape;
            body.createFixture(fixtureDef);
        }
        polyShape.dispose();
    }
}

