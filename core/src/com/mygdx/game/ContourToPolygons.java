package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Bart on 14/05/2015.
 */
public class ContourToPolygons{
    IntArray discardedPoints;
    Array<Vector2> thePoly;
    Array<Array<Vector2>> result;

    public void BuildShape(Body body, FixtureDef fixtureDef,Array<Vector2> vertices){
        discardedPoints = new IntArray();
        thePoly = vertices;
        result = new Array();
        PolygonShape polyShape;

        triangulate();

        for(int i = 0 ; i < result.size ; i++)
        {
            Array<Vector2> vec = result.get(i);
            Vector2[] verticesVec = vec.toArray(Vector2.class);

            polyShape = new PolygonShape();
            polyShape.set(verticesVec);
            fixtureDef.shape = polyShape;
            body.createFixture(fixtureDef);
        }
    }

    public void triangulate(){
        // success is a Boolean variable which will say if we found a valid triangle
        boolean success = true;
        // triangleA is the leftmost vertex of the polygon, according to discarded points
        int triangleA = leftmostPoint();
        // triangleB is next vertex
        int triangleB = (triangleA + 1) % thePoly.size;
        // triangleC is previous vertex so in the end we have a triangle
        int triangleC = (triangleA - 1);
        if (triangleC<0) {
            triangleC = thePoly.size - 1;
        }

        // now it's time to see if any of the remaining vertices is inside the triangle
        for (int i = 0 ; i < thePoly.size ; i++) {
            if (i != triangleA && i != triangleB && i != triangleC) {
                if (isInsideTriangle(thePoly.get(triangleA), thePoly.get(triangleB), thePoly.get(triangleC), thePoly.get(i))) {
                    // if one vertex is inside the triangle, we discard the leftmost point just found
                    discardedPoints.add(triangleA);
                    // then we set success variable to false
                    success = false;
                    break;
                }
            }
        }
        if (success) {
            // if we have just found a valid triangle, we draw it
            //drawTriangle(triangleA,triangleB,triangleC);
            Array<Vector2> v = new Array();
            v.add(new Vector2(thePoly.get(triangleA)));
            v.add(new Vector2(thePoly.get(triangleB)));
            v.add(new Vector2(thePoly.get(triangleC)));
            result.add(v);

            // then we remove the leftmost point found from the polygon, obtaining a smaller polygon
            thePoly.removeIndex(triangleA);
            // we also clear the vector of discarded points
            discardedPoints = new IntArray();
        }
        // if there are still more than three points in the polygon (it's not a triangle) then execute triangulate function once more
        if (thePoly.size > 3) {
            triangulate();
        }
        else {
            // otherwise draw the remaining triangle
            //drawTriangle(0,1,2);
        }
    }

    // function to find the leftmost point
    private int leftmostPoint(){
        int minIndex = 0;
        // first, look for the first undiscarded point
        for (int i = 0 ; i < thePoly.size; i++) {
            if (discardedPoints.indexOf(i) == -1) {
                minIndex = i;
                break;
            }
        }
        // then check for all undiscarded points to find the one with the lowest x value (the leftmost)
        for (int i = 0 ; i < thePoly.size ; i++) {
            if (discardedPoints.indexOf(i) == -1 && thePoly.get(i).x < thePoly.get(minIndex).x) {
                minIndex = i;
            }
        }
        return minIndex;
    }
    // these two functions have already been explained in the post "Algorithm to determine if a point is inside a triangle with mathematics (no hit test involved)"
    private boolean isInsideTriangle(Vector2 A, Vector2 B ,Vector2 C ,Vector2 P) {
        float planeAB = (A.x-P.x)*(B.y-P.y)-(B.x-P.x)*(A.y-P.y);
        float planeBC = (B.x-P.x)*(C.y-P.y)-(C.x - P.x)*(B.y-P.y);
        float planeCA = (C.x-P.x)*(A.y-P.y)-(A.x - P.x)*(C.y-P.y);
        return sign(planeAB)==sign(planeBC) && sign(planeBC)==sign(planeCA);
    }
    private int sign(float n){
        return (n > 0 ? 1 : -1);
    }
}

