package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bart on 17/05/2015.
 */
public class GravityField {
    Vector2 [][] forces;
    float unitsize;
    Vector2 gridcenter;//Bottom left actually
    int cellamount;

    public boolean debugDrawGrid;
    public boolean debugDrawForces;

    // @center : center of the grid
    // @amount : amount of grid cells per side
    // @cellsize : size of the side of a cell
    public GravityField(Vector2 center, float edgesize, int amount){
        forces = new Vector2[amount][amount];
        unitsize = edgesize/amount;
        gridcenter = center;
        cellamount = amount;
        debugDrawForces = true;
        debugDrawGrid = true;
        clearField();
    }

    public void clearField(){
        for(int x = 0 ; x < cellamount ; x++)
            for(int y = 0 ; y < cellamount ; y++){
                forces[x][y] = new Vector2(0,0);
            }
    }

    public void addSphericalAttractor(Vector2 center){
        //Compute contribution to field in each grid point
        for(int x = 0 ; x < cellamount ; x++)
            for(int y = 0 ; y < cellamount ; y++){
                // Grid point position
                Vector2 localpoint = new Vector2(x * unitsize + gridcenter.x, y * unitsize + gridcenter.y);
                // Vector from grid point to center
                localpoint.x = center.x - localpoint.x;
                localpoint.y = center.y - localpoint.y;
                // Compute length
                float r = localpoint.len();
                // Gravitationnal constant * mass1 * mass2
                float Gm1m2 = 0.005f;

                if(r < 0.00001f)
                    localpoint.setLength(0.f);
                else {
                    float length = Gm1m2 / (r * r);
                    if (length > 0.1f)
                        length = 0.1f;
                    localpoint.setLength(length);
                }

                forces[x][y].x += localpoint.x;
                forces[x][y].y += localpoint.y;
            }
    }

    public Vector2 getForce(float x, float y){
        int x0 = fastfloor((x - gridcenter.x) / unitsize);
        int y0 = fastfloor((y - gridcenter.y) / unitsize);

        // Outside the grid, no force is applied
        if (x0 < 0 || x0 > cellamount-2 ||
            y0 < 0 || y0 > cellamount-2) {
            return new Vector2(0,0);
        }

        Vector2 t = new Vector2();
        // Get the internal distance to each of the four cell points
        // This distance is between 0 and 1
        t.set(x - x0,y - y0);
        float d00 = t.len();
        t.set(x - x0,y - y0 + 1);
        float d01 = t.len();
        t.set(x - x0 + 1,y - y0);
        float d10 = t.len();
        t.set(x - x0 + 1,y - y0 + 1);
        float d11 = t.len();


        Vector2 force = new Vector2();
        force.x = (forces[x0][y0].x     * d00 +
                   forces[x0+1][y0].x   * d10 +
                   forces[x0][y0+1].x   * d10 +
                   forces[x0+1][y0+1].x * d10)/(d00 + d10 + d01 + d11);
        force.y = (forces[x0][y0].y     * d00 +
                   forces[x0+1][y0].y   * d10 +
                   forces[x0][y0+1].y   * d10 +
                   forces[x0+1][y0+1].y * d10)/(d00 + d10 + d01 + d11);

        // Utiliser couleur de l'objet et couleur actuelle pour obtenir force finale
        // todo

        // Return final force value
        return force;
    }

    public void debug_draw(ShapeRenderer renderer, Matrix4 combined){

        //Display grid
        if(debugDrawGrid){
            renderer.setColor(1, 1, 0, 1);
            for(int i = 0 ; i < cellamount ; i++) {
                float x1 = i * unitsize + gridcenter.x;
                float x2 = i * unitsize + gridcenter.x;
                float y1 = 0 * unitsize + gridcenter.y;
                float y2 = (cellamount - 1) * unitsize + gridcenter.y;
                renderer.line(x1,y1,x2,y2);
                x1 = 0 * unitsize + gridcenter.x;
                x2 = (cellamount - 1) * unitsize + gridcenter.x;
                y1 = i * unitsize + gridcenter.y;
                y2 = i * unitsize + gridcenter.y;
                renderer.line(x1,y1,x2,y2);
            }
        }

        // Display forces
        if(debugDrawForces){
            renderer.setColor(1, 1, 1, 1);
            for(int x = 0 ; x < cellamount ; x++)
                for(int y = 0 ; y < cellamount ; y++) {
                float x1 = x * unitsize + gridcenter.x;
                float y1 = y * unitsize + gridcenter.y;
                float x2 = x1 + forces[x][y].x*10;
                float y2 = y1 + forces[x][y].y*10;
                renderer.line(x1,y1,x2,y2);
            }
        }
    }

    private int fastfloor(float n)
    {
        return (n >= 0) ? (int)(n) : (int)(n-1);
    }
}
